package com.imadkrouni.barq.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProject(id: Long): Project?

    @Insert
    suspend fun insertProject(p: Project): Long

    @Update
    suspend fun updateProject(p: Project)

    @Delete
    suspend fun deleteProject(p: Project)

    // Rooms
    @Query("SELECT * FROM rooms WHERE projectId = :pid")
    fun getRooms(pid: Long): Flow<List<RoomEntity>>

    @Insert
    suspend fun insertRoom(r: RoomEntity): Long

    @Delete
    suspend fun deleteRoom(r: RoomEntity)

    // Components
    @Query("SELECT * FROM components WHERE projectId = :pid")
    fun getComponents(pid: Long): Flow<List<ElectricalComponent>>

    @Insert
    suspend fun insertComponent(c: ElectricalComponent): Long

    @Update
    suspend fun updateComponent(c: ElectricalComponent)

    @Delete
    suspend fun deleteComponent(c: ElectricalComponent)

    @Query("UPDATE components SET isOn = :on WHERE id = :id")
    suspend fun setComponentState(id: Long, on: Boolean)

    // Wires
    @Query("SELECT * FROM wires WHERE projectId = :pid")
    fun getWires(pid: Long): Flow<List<Wire>>

    @Insert
    suspend fun insertWire(w: Wire): Long

    @Query("DELETE FROM wires WHERE projectId = :pid")
    suspend fun clearWires(pid: Long)
}
