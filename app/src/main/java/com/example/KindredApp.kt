package com.example

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClient
import com.example.data.repository.BabyNameRepository
import com.example.data.session.SessionStore
import com.example.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class KindredApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = container.sync.onForeground()
            override fun onStop(owner: LifecycleOwner) = container.sync.onBackground()
        })
    }
}

class AppContainer(app: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val db = AppDatabase.getInstance(app)
    val session = SessionStore(app)
    val api = ApiClient(BuildConfig.SERVER_URL, tokenProvider = { session.session.value.token })
    val repository = BabyNameRepository(db.appDao())
    val sync = SyncManager(db.appDao(), api, session, scope)
}
