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

    fun createProject(p: Project, onDone: (Long) -> Unit) = viewModelScope.launch {
        val id = dao.insertProject(p)
        onDone(id)
    }

    fun selectProject(id: Long) = viewModelScope.launch {
        val p = dao.getProject(id)
        _currentProject.value = p
        loadProjectData(id)
    }

    private fun loadProjectData(id: Long) {
        viewModelScope.launch {
            dao.getRooms(id).collect { _rooms.value = it }
        }
        viewModelScope.launch {
            dao.getComponents(id).collect { _components.value = it }
        }
        viewModelScope.launch {
            dao.getWires(id).collect { _wires.value = it }
        }
    }

    fun addRoom(r: RoomEntity) = viewModelScope.launch { dao.insertRoom(r) }

    fun addComponent(c: ElectricalComponent) = viewModelScope.launch {
        dao.insertComponent(c)
    }

    fun toggleComponent(id: Long, on: Boolean) = viewModelScope.launch {
        dao.setComponentState(id, on)
    }

    fun toggleAllInRoom(roomId: Long, on: Boolean) = viewModelScope.launch {
        _components.value.filter { it.roomId == roomId }.forEach {
            dao.setComponentState(it.id, on)
        }
    }

    fun deleteComponent(c: ElectricalComponent) = viewModelScope.launch {
        dao.deleteComponent(c)
    }

    fun saveWire(w: Wire) = viewModelScope.launch { dao.insertWire(w) }

    fun clearWires() = viewModelScope.launch {
        _currentProject.value?.let { dao.clearWires(it.id) }
    }

    fun updateProject(p: Project) = viewModelScope.launch { dao.updateProject(p) }

    fun deleteProject(p: Project) = viewModelScope.launch { dao.deleteProject(p) }
}
