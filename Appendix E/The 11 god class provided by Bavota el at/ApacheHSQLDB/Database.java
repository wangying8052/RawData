/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2004, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import java.io.IOException;

import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.HsqlNameManager.HsqlName;

// fredt@users 20020130 - patch 476694 by velichko - transaction savepoints
// additions to different parts to support savepoint transactions
// fredt@users 20020215 - patch 1.7.0 - new HsqlProperties class
// support use of properties from database.properties file
// fredt@users 20020218 - patch 1.7.0 - DEFAULT keyword
// support for default values for table columns
// fredt@users 20020305 - patch 1.7.0 - restructuring
// some methods move to Table.java, some removed
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - restructuring
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - error trapping
// boucherb@users 20020130 - patch 1.7.0 - use lookup for speed
// idents listed in alpha-order for easy check of stats...
// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support
// boucherb@users - doc 1.7.0 - added javadoc comments
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg
// tony_lai@users 20020820 - changes to shutdown compact to save memory
// boucherb@users 20020828 - allow reconnect to local db that has shutdown
// fredt@users 20020912 - patch 1.7.1 by fredt - drop duplicate name triggers
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else if(){} chains with switch()
// boucherb@users 20020310 - class loader update for JDK 1.1 compliance
// fredt@users 20030401 - patch 1.7.2 by akede@users - data files readonly
// fredt@users 20030401 - patch 1.7.2 by Brendan Ryan - data files in Jar
// boucherb@users 20030405 - removed 1.7.2 lint - updated JavaDocs
// boucherb@users 20030425 - DDL methods are moved to DatabaseCommandInterpreter.java
// boucherb@users - fredt@users 200305..200307 - patch 1.7.2 - DatabaseManager upgrade
// loosecannon1@users - patch 1.7.2 - properties on the JDBC URL

/**
 *  Database is the root class for HSQL Database Engine database. <p>
 *
 *  Although it either directly or indirectly provides all or most of the
 *  services required for DBMS functionality, this class should not be used
 *  directly by an application. Instead, to achieve portability and
 *  generality, the JDBC interface classes should be used.
 *
 * @version  1.7.2
 */
public class Database {

    int            databaseID;
    private String sType;
    private String sName;

// loosecannon1@users 1.7.2 patch properties on the JDBC URL
    private HsqlProperties urlProperties;
    private String         sPath;
    boolean                isNew;
    private UserManager    userManager;
    private HsqlArrayList  tTable;
    DatabaseInformation    dInfo;
    ClassLoader            classLoader;

    /** indicates the state of the database */
    private int dbState;
    Logger      logger;

    /** true means that all tables are readonly. */
    boolean databaseReadOnly;

    /**
     * true means that all CACHED and TEXT tables are readonly.
     *  MEMORY tables are updatable but updates are not persisted.
     */
    private boolean filesReadOnly;

    /** true means filesReadOnly but CACHED and TEXT tables are disallowed */
    private boolean                filesInJar;
    boolean                        sqlEnforceSize;
    boolean                        sqlEnforceStrictSize;
    int                            firstIdentity;
    private HashMap                hAlias;
    private boolean                bIgnoreCase;
    private boolean                bReferentialIntegrity;
    SessionManager                 sessionManager;
    private HsqlDatabaseProperties databaseProperties;
    HsqlNameManager                nameManager;
    DatabaseObjectNames            triggerNameList;
    DatabaseObjectNames            indexNameList;
    DatabaseObjectNames            constraintNameList;
    SequenceManager                sequenceManager;
    CompiledStatementManager       compiledStatementManager;

    //
    static final int DATABASE_ONLINE       = 1;
    static final int DATABASE_OPENING      = 4;
    static final int DATABASE_CLOSING      = 8;
    static final int DATABASE_SHUTDOWN     = 16;
    static final int CLOSEMODE_IMMEDIATELY = -1;
    static final int CLOSEMODE_NORMAL      = 0;
    static final int CLOSEMODE_COMPACT     = 1;
    static final int CLOSEMODE_SCRIPT      = 2;

    

    /**
     * Opens this database.  The database should be opened after construction.
     */
    synchronized void open() throws HsqlException {

        if (!isShutdown()) {
            return;
        }

        reopen();
    }

    /**
     * Opens this database.  The database should be opened after construction.
     * or reopened by the close(int closemode) method during a
     * "shutdown compact". Closes the log if there is an error.
     */
    void reopen() throws HsqlException {

        setState(DATABASE_OPENING);

        try {
            User sysUser;

            isNew = (sType == DatabaseManager.S_MEM
                     ||!FileUtil.exists(sPath + ".properties",
                                        isFilesInJar(), getClass()));
            databaseProperties = new HsqlDatabaseProperties(this);

            databaseProperties.load();
            databaseProperties.setURLProperties(urlProperties);
            compiledStatementManager.reset();

            tTable                = new HsqlArrayList();
            userManager           = new UserManager();
            hAlias                = Library.getAliasMap();
            nameManager           = new HsqlNameManager();
            triggerNameList       = new DatabaseObjectNames();
            indexNameList         = new DatabaseObjectNames();
            constraintNameList    = new DatabaseObjectNames();
            sequenceManager       = new SequenceManager();
            bReferentialIntegrity = true;
            sysUser               = userManager.createSysUser(this);
            sessionManager        = new SessionManager(this, sysUser);
            dInfo = DatabaseInformation.newDatabaseInformation(this);

            if (sType != DatabaseManager.S_MEM) {
                logger.openLog(this);
            }

            if (isNew) {
                sessionManager.getSysSession().sqlExecuteDirectNoPreChecks(
                    "CREATE USER SA PASSWORD \"\" ADMIN");
                logger.synchLogForce();
            }

            dInfo.setWithContent(true);
        } catch (Throwable e) {
            logger.closeLog(Database.CLOSEMODE_IMMEDIATELY);
            logger.releaseLock();
            setState(DATABASE_SHUTDOWN);
            clearStructures();

            if (!(e instanceof HsqlException)) {
                e = Trace.error(Trace.GENERAL_ERROR, e.toString());
            }

            throw (HsqlException) e;
        }

        setState(DATABASE_ONLINE);
    }

    /**
     * Clears the data structuress, making them elligible for garbage collection.
     */
    void clearStructures() {

        if (tTable != null) {
            for (int i = 0; i < tTable.size(); i++) {
                Table table = (Table) tTable.get(i);

                table.dropTriggers();
            }
        }

        isNew              = false;
        tTable             = null;
        userManager        = null;
        hAlias             = null;
        nameManager        = null;
        triggerNameList    = null;
        constraintNameList = null;
        indexNameList      = null;
        sequenceManager    = null;
        sessionManager     = null;
        dInfo              = null;
    }

    /**
     *  Returns the type of the database: "mem", "file", "res"
     */
    String getType() {
        return sType;
    }

    /**
     *  Returns the path of the database
     */
    String getPath() {
        return sPath;
    }

    /**
     *  Returns the database properties.
     */
    HsqlDatabaseProperties getProperties() {
        return databaseProperties;
    }

    /**
     *  Returns true if database has been shut down, false otherwise
     */
    synchronized boolean isShutdown() {
        return dbState == DATABASE_SHUTDOWN;
    }

    /**
     *  Constructs a new Session that operates within (is connected to) the
     *  context of this Database object. <p>
     *
     *  If successful, the new Session object initially operates on behalf of
     *  the user specified by the supplied user name.
     *
     * Throws if username or password is invalid.
     */
    synchronized Session connect(String username,
                                 String password) throws HsqlException {

        User user = userManager.getUser(username, password);
        Session session = sessionManager.newSession(this, user,
            databaseReadOnly);

        logger.logConnectUser(session);

        return session;
    }

    /**
     *  Puts this Database object in global read-only mode. After
     *  this call, all existing and future sessions are limited to read-only
     *  transactions. Any following attempts to update the state of the
     *  database will result in throwing an HsqlException.
     */
    void setReadOnly() {
        databaseReadOnly = true;
        filesReadOnly    = true;
    }

    /**
     * After this call all CACHED and TEXT tables will be set to read-only
     * mode. Changes to MEMORY tables will NOT
     * be stored or updated in the script file. This mode is intended for
     * use with read-only media where data should not be persisted.
     */
    void setFilesReadOnly() {
        filesReadOnly = true;
    }

    /**
     * Is this in filesReadOnly mode?
     */
    boolean isFilesReadOnly() {
        return filesReadOnly;
    }

    /**
     * Is this in filesInJar mode?
     */
    public boolean isFilesInJar() {
        return filesInJar;
    }


    /**
     *  Returns the UserManager for this Database.
     */
    UserManager getUserManager() {
        return userManager;
    }

    /**
     *  Sets the isReferentialIntegrity attribute.
     */
    public void setReferentialIntegrity(boolean ref) {
        bReferentialIntegrity = ref;
    }

    /**
     *  Is referential integrity currently enforced?
     */
    boolean isReferentialIntegrity() {
        return bReferentialIntegrity;
    }

    /**
     *  Returns a map from Java method-call name aliases to the
     *  fully-qualified names of the Java methods themsleves.
     */
    HashMap getAliasMap() {
        return hAlias;
    }

    /**
     *  Returns the fully qualified name for the Java method corresponding to
     *  the given method alias. If there is no Java method, then returns the
     *  alias itself.
     */
    String getJavaName(String s) {

        String alias = (String) hAlias.get(s);

        return (alias == null) ? s
                               : alias;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// temp tables should be accessed by the owner and not scripted in the log

    /**
     *  Retruns the specified user-defined table or view visible within the
     *  context of the specified Session, or any system table of the given
     *  name. It excludes any temp tables created in different Sessions.
     *  Throws if the table does not exist in the context.
     */
    public Table getTable(Session session, String name) throws HsqlException {

        Table t = findUserTable(session, name);

        if (t == null) {
            t = dInfo.getSystemTable(session, name);
        }

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    /**
     *  Retruns the specified user-defined table or view visible within the
     *  context of the specified Session. It excludes system tables and
     *  any temp tables created in different Sessions.
     *  Throws if the table does not exist in the context.
     */
    Table getUserTable(Session session, String name) throws HsqlException {

        Table t = findUserTable(session, name);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    /**
     *  Registers the specified table or view with this Database.
     */
    void linkTable(Table t) {
        tTable.add(t);
    }

    /**
     * Sets the database to treat any new VARCHAR column declarations as
     * VARCHAR_IGNORECASE.
     */
    void setIgnoreCase(boolean b) {
        bIgnoreCase = b;
    }

    /**
     *  Does the database treat any new VARCHAR column declarations as
     * VARCHAR_IGNORECASE.
     */
    boolean isIgnoreCase() {
        return bIgnoreCase;
    }

    /**
     * Returns the table that has an index with the given name in the
     * whole database and is visible in this session.
     * Returns null if not found.
     */
    Table findUserTableForIndex(Session session, String name) {

        HsqlName hsqlname = indexNameList.getOwner(name);

        if (hsqlname == null) {
            return null;
        }

        return findUserTable(session, hsqlname.name);
    }

    /**
     *  Returns index of a table or view in the HsqlArrayList that
     *  contains the table objects for this Database.
     *
     * @param  table the Table object
     * @return  the index of the specified table or view, or -1 if not found
     */
    int getTableIndex(Table table) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.get(i);

            if (t == table) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Drops the index with the specified name from this database.
     * @param indexname the name of the index to drop
     * @param  ifExists if true and if the Index to drop does not exist, fail
     *      silently, else throw
     * @param session the execution context
     * @throws HsqlException if the index does not exist, the session lacks
     *        the permission or the operation violates database integrity
     */
    void dropIndex(Session session, String indexname, String tableName,
                   boolean ifExists) throws HsqlException {

        Table t = findUserTableForIndex(session, indexname);

        if (t == null) {
            if (ifExists) {
                return;
            } else {
                throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
            }
        }

        if (tableName != null &&!t.getName().name.equals(tableName)) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        t.checkDropIndex(indexname, null);

// fredt@users 20020405 - patch 1.7.0 by fredt - drop index bug
// see Table.moveDefinition();
        session.commit();
        session.setScripting(!t.isTemp());

        TableWorks tw = new TableWorks(session, t);

        tw.dropIndex(indexname);
    }

    /**
     * Returns the SessionManager for the database.
     */
    SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     *  Called by the garbage collector on this Databases object when garbage
     *  collection determines that there are no more references to it.
     */
    protected void finalize() {

        if (getState() != DATABASE_ONLINE) {
            return;
        }

        try {
            close(CLOSEMODE_IMMEDIATELY);
        } catch (HsqlException e) {    // it's too late now
        }
    }

    /**
     *  Closes this Database using the specified mode. <p>
     *
     * <ol>
     *  <LI> closemode -1 performs SHUTDOWN IMMEDIATELY, equivalent
     *       to  a poweroff or crash.
     *
     *  <LI> closemode 0 performs a normal SHUTDOWN that
     *      checkpoints the database normally.
     *
     *  <LI> closemode 1 performs a shutdown compact that scripts
     *       out the contents of any CACHED tables to the log then
     *       deletes the existing *.data file that contains the data
     *       for all CACHED table before the normal checkpoint process
     *       which in turn creates a new, compact *.data file.
     * </ol>
     */
    void close(int closemode) throws HsqlException {

        HsqlException he = null;

        setState(DATABASE_CLOSING);
        sessionManager.closeAllSessions();
        sessionManager.clearAll();

        // fredt - impact of possible error conditions in closing the log
        // should be investigated for the CLOSEMODE_COMPACT mode
        logger.closeLog(closemode);

        try {
            if (closemode == CLOSEMODE_COMPACT &&!filesReadOnly) {
                clearStructures();
                reopen();
                setState(DATABASE_CLOSING);
                logger.closeLog(CLOSEMODE_NORMAL);
            }
        } catch (Throwable t) {
            if (t instanceof HsqlException) {
                he = (HsqlException) t;
            } else {
                he = Trace.error(Trace.GENERAL_ERROR, t.toString());
            }
        }

        classLoader = null;

        logger.releaseLock();
        setState(DATABASE_SHUTDOWN);
        clearStructures();

        // fredt - this could change to avoid removing a db from the
        // DatabaseManager repository if there are pending getDatabase()
        // calls
        DatabaseManager.removeDatabase(this);

        if (he != null) {
            throw he;
        }
    }


    /**
     * Throws if the column is referenced in a view.
     */
    void checkColumnIsInView(String table,
                             String column) throws HsqlException {

        View[] views = getViewsWithTable(table, column);

        if (views != null) {
            throw Trace.error(Trace.COLUMN_IS_REFERENCED,
                              views[0].getName().name);
        }
    }

    /**
     * Returns an array of views that reference another view.
     */
    private View[] getViewsWithView(View view) {

        HsqlArrayList list = null;

        for (int i = 0; i < tTable.size(); i++) {
            Table t = (Table) tTable.get(i);

            if (t.isView()) {
                boolean found = ((View) t).hasView(view);

                if (found) {
                    if (list == null) {
                        list = new HsqlArrayList();
                    }

                    list.add(t);
                }
            }
        }

        return list == null ? null
                            : (View[]) list.toArray(new View[list.size()]);
    }

    /**
     * Returns an array of views that reference the specified table or
     * the specified column if column parameter is not null.
     */
    private View[] getViewsWithTable(String table, String column) {

        HsqlArrayList list = null;

        for (int i = 0; i < tTable.size(); i++) {
            Table t = (Table) tTable.get(i);

            if (t.isView()) {
                boolean found = column == null ? ((View) t).hasTable(table)
                                               : ((View) t).hasColumn(table,
                                                   column);

                if (found) {
                    if (list == null) {
                        list = new HsqlArrayList();
                    }

                    list.add(t);
                }
            }
        }

        return list == null ? null
                            : (View[]) list.toArray(new View[list.size()]);
    }

    /**
     * Returns an array of views that reference a sequence.
     */
    View[] getViewsWithSequence(NumberSequence sequence) {

        HsqlArrayList list = null;

        for (int i = 0; i < tTable.size(); i++) {
            Table t = (Table) tTable.get(i);

            if (t.isView()) {
                boolean found = ((View) t).hasSequence(sequence);

                if (found) {
                    if (list == null) {
                        list = new HsqlArrayList();
                    }

                    list.add(t);
                }
            }
        }

        return list == null ? null
                            : (View[]) list.toArray(new View[list.size()]);
    }

    /**
     * After addition or removal of columns and indexes all views that
     * reference the table should be recompiled.
     */
    void recompileViews(String table) throws HsqlException {

        View[] viewlist = getViewsWithTable(table, null);

        if (viewlist != null) {
            for (int i = 0; i < viewlist.length; i++) {
                viewlist[i].compile();
            }
        }
    }

    /**
     *  Removes any foreign key Constraint objects (exported keys) held by any
     *  tables referenced by the specified table. <p>
     *
     *  This method is called as the last step of a successful call to
     *  dropTable() in order to ensure that the dropped Table ceases to be
     *  referenced when enforcing referential integrity.
     *
     * @param  toDrop The table to which other tables may be holding keys.
     *      This is a table that is in the process of being dropped.
     */
    void removeExportedKeys(Table toDrop) {

        for (int i = 0; i < tTable.size(); i++) {
            Table table = (Table) tTable.get(i);

            for (int j = table.constraintList.length - 1; j >= 0; j--) {
                Table refTable = table.constraintList[j].getRef();

                if (toDrop == refTable) {
                    table.constraintList =
                        (Constraint[]) ArrayUtil.toAdjustedArray(
                            table.constraintList, null, j, -1);
                }
            }
        }
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Drops a trigger with the specified name in the given context.
     */
    void dropTrigger(Session session, String name) throws HsqlException {

        boolean found = triggerNameList.containsName(name);

        Trace.check(found, Trace.TRIGGER_NOT_FOUND, name);

        HsqlName tableName = (HsqlName) triggerNameList.removeName(name);
        Table    t         = this.findUserTable(session, tableName.name);

        t.dropTrigger(name);
        session.setScripting(!t.isTemp());
    }

    /**
     * Ensures system table producer's table cache, if it exists, is set dirty.
     * After this call up-to-date versions are generated in response to
     * system table requests. <p>
     *
     * Also resets all prepared statements if a change to database structure
     * can possibly affect any existing prepared statement's validity.<p>
     *
     * The argument is false if the change to the database structure does not
     * affect the prepared statement, such as when a new table is added.<p>
     *
     * The argument is typically true when a database object is dropped,
     * altered or a permission was revoked.
     *
     * @param  resetPrepared If true, reset all prepared statements.
     */
    void setMetaDirty(boolean resetPrepared) {

        if (dInfo != null) {
            dInfo.setDirty();
        }

        if (resetPrepared) {
            this.compiledStatementManager.resetStatements();
        }
    }

// boucherb@users - patch 1.7.2 - system change number support
// fredt@users - system change numbers utilised

    /** last statement level change number - not externally settable */
    private long dbSCN = 0;

    /** last statement level change number for DDL statements - unused */
    private long ddlSCN = 0;

    /** last statement level change number for DML statements - used for all statements */
    private long dmlSCN = 0;


    private synchronized void setState(int state) {
        dbState = state;
    }

    synchronized int getState() {
        return dbState;
    }

    String getStateString() {

        int state = getState();

        switch (state) {

            case DATABASE_CLOSING :
                return "DATABASE_CLOSING";

            case DATABASE_ONLINE :
                return "DATABASE_ONLINE";

            case DATABASE_OPENING :
                return "DATABASE_OPENING";

            case DATABASE_SHUTDOWN :
                return "DATABASE_SHUTDOWN";

            default :
                return "UNKNOWN";
        }
    }

// boucherb@users - 200403?? - patch 1.7.2 - metadata
//------------------------------------------------------------------------------
    private String uri;

    /**
     * Retrieves the uri portion of this object's in-process JDBC url.
     *
     * @return the uri portion of this object's in-process JDBC url
     */
    public String getURI() {
        return sName;
    }
}
