package com.ucua.ucua.config

import org.hibernate.community.dialect.SQLiteDialect
import org.hibernate.dialect.identity.IdentityColumnSupport
import org.hibernate.dialect.identity.IdentityColumnSupportImpl

/**
 * Custom SQLite dialect for Hibernate 6 / Spring Boot 3 compatibility.
 */
class UcuaSQLiteDialect : SQLiteDialect() {

    override fun getIdentityColumnSupport(): IdentityColumnSupport {
        return SQLiteIdentityColumnSupport()
    }

    class SQLiteIdentityColumnSupport : IdentityColumnSupportImpl() {
        override fun supportsIdentityColumns(): Boolean = true
        override fun supportsInsertSelectIdentity(): Boolean = true
        override fun hasDataTypeInIdentityColumn(): Boolean = false
        override fun getIdentitySelectString(table: String?, column: String?, type: Int): String = "select last_insert_rowid()"
        override fun getIdentityColumnString(type: Int): String = "integer"
    }
}
