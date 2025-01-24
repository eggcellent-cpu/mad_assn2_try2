package com.it2161.dit233000D.movieviewer

import android.app.Application
import android.util.Log
import com.it2161.dit233000D.movieviewer.data.movie.MovieDatabase
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.user.UserDatabase

class MovieViewerApplication : Application() {

    // Initialize MovieDatabase
    val movieDatabase: MovieDatabase by lazy {
        MovieDatabase.getDatabase(applicationContext)
    }

    // Initialize UserDatabase
    val userDatabase: UserDatabase by lazy {
        UserDatabase.getDatabase(applicationContext)
    }

    val movieRepository: MovieRepository by lazy {
        MovieRepository.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MovieViewerApplication", "Application Created!")
        instance = this // Assign the application instance
    }

    companion object {
        // Singleton instance of the application
        lateinit var instance: MovieViewerApplication
            private set
    }
}
