package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val logoUrl: String,
    val streamUrl: String,
    val backupUrl: String,
    val country: String,
    val language: String,
    val isFavorite: Boolean,
    val isEnabled: Boolean,
    val sortOrder: Int,
    val viewsCount: Int
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subtitle: String,
    val channelCount: Int,
    val iconUrl: String,
    val categoryType: String,
    val gradientColorHex: String
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val leagueName: String,
    val leagueIconUrl: String,
    val homeTeam: String,
    val homeLogoUrl: String,
    val awayTeam: String,
    val awayLogoUrl: String,
    val matchTime: String,
    val matchDate: String,
    val status: String,
    val homeScore: Int,
    val awayScore: Int,
    val streamUrl: String,
    val isLive: Boolean,
    val isFavorite: Boolean
)

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val type: String, // MOVIE, SERIES, ANIME
    val year: String,
    val rating: String,
    val genre: String,
    val description: String,
    val duration: String,
    val seasonsCount: Int,
    val episodesCount: Int,
    val streamUrl: String,
    val isTop: Boolean,
    val topRank: String,
    val isFavorite: Boolean
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val itemId: String,
    val itemType: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val streamUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_sources")
data class ApiSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val endpoint: String,
    val apiKey: String,
    val headers: String,
    val isEnabled: Boolean,
    val status: String,
    val lastChecked: String
)
