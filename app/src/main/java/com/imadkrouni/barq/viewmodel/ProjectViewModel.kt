package com.imadkrouni.barq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imadkrouni.barq.BarqApp
import com.imadkrouni.barq.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as BarqApp).database.dao()

    val projects: StateFlow<List<Project>> =
        dao.getAllProjects().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomEntity>>(emptyList())
    val rooms: StateFlow<List<RoomEntity>> = _rooms.asStateFlow()

    private val _components = MutableStateFlow<List<ElectricalComponent>>(emptyList())
    val components: StateFlow<List<ElectricalComponent>> = _components.asStateFlow()

    private val _wires = MutableStateFlow<List<Wire>>(emptyList())
    val wires: StateFlow<List<Wire>> = _wires.asStateFlow()

    // Undo history
    private val undoStack = mutableListOf<List<ElectricalComponent>>()
    private val redoStack = mutableListOf<List<ElectricalComponent>>()

    fun createProject(p: Project, onDone: (Long) -> Unit) = viewModelScope.launch {
        onDone(dao.insertProject(p))
    }

    fun selectProject(id: Long) = viewModelScope.launch {
        _currentProject.value = dao.getProject(id)
        launch { dao.getRooms(id).collect { _rooms.value = it } }
        launch { dao.getComponents(id).collect { _components.value = it } }
        launch { dao.getWires(id).collect { _wires.value = it } }
    }

    fun addRoom(r: RoomEntity) = viewModelScope.launch { dao.insertRoom(r) }

    fun deleteRoom(r: RoomEntity) = viewModelScope.launch { dao.deleteRoom(r) }

    fun addComponent(c: ElectricalComponent) {
        pushUndo()
        viewModelScope.launch { dao.insertComponent(c) }
    }

    fun updateComponent(c: ElectricalComponent) {
        viewModelScope.launch { dao.updateComponent(c) }
    }

    fun deleteComponent(c: ElectricalComponent) {
        pushUndo()
        viewModelScope.launch { dao.deleteComponent(c) }
    }

    fun duplicateComponent(c: ElectricalComponent) {
        pushUndo()
        viewModelScope.launch {
            dao.insertComponent(c.copy(id = 0, x = c.x + 80f, y = c.y + 80f))
        }
    }

    fun toggleComponent(id: Long, on: Boolean) = viewModelScope.launch {
        dao.setComponentState(id, on)
    }

    fun toggleAll(on: Boolean) = viewModelScope.launch {
        _components.value.forEach { dao.setComponentState(it.id, on) }
    }

    fun toggleRoom(roomId: Long, on: Boolean) = viewModelScope.launch {
        _components.value.filter { it.roomId == roomId }.forEach {
            dao.setComponentState(it.id, on)
        }
    }

    // ============ سيناريوهات ============
    fun applyScenario(name: String) = viewModelScope.launch {
        val comps = _components.value
        when (name) {
            "ALL_ON"  -> comps.forEach { dao.setComponentState(it.id, true) }
            "ALL_OFF" -> comps.forEach { dao.setComponentState(it.id, false) }
            "SLEEP"   -> comps.forEach {
                val isLight = it.type.contains("LIGHT") || it.type == "CHANDELIER"
                val isBedroom = it.label.contains("نوم", ignoreCase = true)
                dao.setComponentState(it.id, isLight && isBedroom)
            }
            "PARTY"   -> comps.forEach {
                val isLight = it.type.contains("LIGHT") || it.type == "CHANDELIER"
                dao.setComponentState(it.id, isLight)
            }
            "CINEMA"  -> comps.forEach {
                val isLight = it.type.contains("LIGHT")
                val isKitchen = it.label.contains("مطبخ", ignoreCase = true)
                dao.setComponentState(it.id, isLight && !isKitchen)
            }
            "ENTRY"   -> comps.forEach { dao.setComponentState(it.id, false) }
        }
    }

    fun clearWires() = viewModelScope.launch {
        _currentProject.value?.let { dao.clearWires(it.id) }
    }

    fun saveWire(w: Wire) = viewModelScope.launch { dao.insertWire(w) }

    fun updateProject(p: Project) = viewModelScope.launch {
        dao.updateProject(p.copy(updatedAt = System.currentTimeMillis()))
    }

    fun deleteProject(p: Project) = viewModelScope.launch { dao.deleteProject(p) }

    // Undo
    private fun pushUndo() {
        undoStack.add(_components.value.toList())
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
    }
}
