/*
 * Copyright 2013 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.hear.rate.monitor.shared.dao.dao;

import android.content.Context;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * generic DAO implementation to retrieve a DAO object and retrieving certain data from the DB.
 * In order to extend from this class you should implement a default constructor which calls the
 * generic dao's constructor <b>GenericDaoImpl(java.lang.Class<T> clazz)</b>
 *
 * @author Dirk Vranckaert
 */
public abstract class Dao<T, ID, H extends DatabaseHelper> implements IDao<T, ID, H> {
    /**
     * Logging
     */
    private static final String LOG_TAG = Dao.class.getSimpleName();

    /**
     * The doa to access all of your entities.
     */
    public com.j256.ormlite.dao.Dao<T, ID> dao;

    private Context context;

    /**
     * This constructor should always be called in order to have a DAO!
     * @param entityClazz The entity-class for which the DAO should be created!
     */
    public Dao(final Class<T> entityClazz, final Class<? extends DatabaseHelper> helperClazz, final Context context) {
        Log.d(LOG_TAG, "Creating DAO for " + entityClazz.getSimpleName() + " from " + getClass().getSimpleName());

        OrmLiteSqliteOpenHelper helper = OpenHelperManager.getHelper(context, helperClazz);
        try {
            dao = helper.getDao(entityClazz);
        } catch (SQLException e) {
            throw new RuntimeException("Could not instantiate a DAO for class " + entityClazz.getName(), e);
        }
        //helper.close();
        //OpenHelperManager.releaseHelper();
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    /**
     * Handles the throwing of fatal exceptions during basic SQL commands.
     * @param e The exception.
     */
    protected void throwFatalException(SQLException e) {
        String message = "An unknown SQL exception occured while executing a basic SQL command!";
        Log.e(LOG_TAG, message, e);
        throw new RuntimeException(message, e);
    }

    /**
     * Get the database helper to give you low-level database access!
     * @return The project-custom database helper ({@link DatabaseHelper}).
     */
    protected DatabaseHelper getDatabaseHelper() {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        return databaseHelper;
    }

    /**
     * @Override
     */
    @Override
    public T findById(ID id) {
        T result = null;
        try {
            result = dao.queryForId(id);
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return result;
    }

    /**
     * @Override
     */
    @Override
    public boolean contains(ID id) {
        try {
            return dao.idExists(id);
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return false;
    }

    /**
     * @Override
     */
    @Override
    public List<T> findAll() {
        List<T> results = null;
        try {
            results = dao.queryForAll();
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return results;
    }

    /**
     * @Override
     */
    @Override
    public T save(T entity) {
        try {
            dao.create(entity);
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return entity;
    }

    /**
     * @Override
     */
    @Override
    public T update(T entity) {
        try {
            dao.update(entity);
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return entity;
    }

    /**
     * @Override
     */
    @Override
    public T saveOrUpdate(T entity) {
        List<T> entities = findAll();
        boolean existingEntity = false;
        for (T savedEntity : entities) {
            if (savedEntity.equals(entity)) {
                existingEntity = true;
                break;
            }
        }

        if (!existingEntity) {
            return save(entity);
        } else {
            return update(entity);
        }
    }

    /**
     * @Override
     */
    @Override
    public void delete(T entity) {
        int result = 0;
        try {
            result = dao.delete(entity);
        } catch (SQLException e) {
            throwFatalException(e);
        }
        Log.d(LOG_TAG, result + " records are deleted!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int refresh(T entity) {
        int result = -1;
        try {
             result = dao.refresh(entity);
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        long result = 0L;
        try {
            result = dao.countOf();
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return result;
    }

    @Override
    public void deleteAll() {
        DeleteBuilder<T, ID> deleteBuilder = dao.deleteBuilder();
        try {
            dao.delete(deleteBuilder.prepare());
        } catch (SQLException e) {
            throwFatalException(e);
        }
    }
}
