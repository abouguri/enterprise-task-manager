package com.jojothemojo.taskmanager.data.remote.task

import retrofit2.http.GET

interface TaskApiService {
    // Only endpoint that exists server-side so far. Don't add POST/PUT/DELETE methods
    // here until the backend actually has them (Phase 4) - a client method for an
    // endpoint that 404s is worse than no method at all.
    @GET("api/tasks")
    suspend fun getTasks(): List<TaskDto>
}
