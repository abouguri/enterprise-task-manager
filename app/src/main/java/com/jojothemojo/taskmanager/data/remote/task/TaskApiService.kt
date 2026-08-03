package com.jojothemojo.taskmanager.data.remote.task

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApiService {
    @GET("api/tasks")
    suspend fun getTasks(): List<TaskDto>

    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequestDto): TaskDto

    // Response<TaskDto>, not a plain return type, because 409 (stale UpdatedAt - see
    // TaskManager-Api AGENT.md §5) is an expected, routinely-handled outcome here, not an
    // exceptional one. Wrapping in Response lets the caller branch on the status code
    // directly instead of relying on catching HttpException for normal control flow.
    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body request: UpdateTaskRequestDto): Response<TaskDto>

    // Response<Unit> for the same reason - 404 (already deleted server-side, or owned by
    // someone else) is expected and handled inline, not exceptional.
    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>
}
