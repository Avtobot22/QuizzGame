package com.quizzgame.app.graph

import com.quizzgame.app.network.AppNetworkGraph
import com.quizzgame.core.session.SessionManager
import java.io.Closeable

class AppGraph internal constructor(val session: SessionManager, val network: AppNetworkGraph) : Closeable {
    override fun close() {
        try {
            network.close()
        } finally {
            session.close()
        }
    }
}
