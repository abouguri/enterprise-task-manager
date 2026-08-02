package com.jojothemojo.taskmanager.data.remote.auth

import com.jojothemojo.taskmanager.domain.model.User
import com.microsoft.identity.client.IAccount

fun IAccount.toDomainUser(): User = User(
    id = id,
    displayName = claims?.get("name") as? String,
    email = username,
)
