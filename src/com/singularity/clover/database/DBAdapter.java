
	
package com.singularity.clover.database;

import java.util.Map;
import java.util.NoSuchElementException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import com.singularity.clover.R;
import com.singularity.clover.Global;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.lbs.LBSBundle;
import com.singularity.clover.entity.wrapper.Scenario;


// TODO ��ӡ��޸�ע��
/**
 * <p> ���ݿ��������࣬�ṩ���ݿ�������ܣ��Լ���ײ�����ݲ����� </p>
 *  <p> ��������ر����ݿ⣬�Լ��Ը����CRUD
 *  ��Create, Retrieve, Update, Delete�������� </p>
 */
public class DBAdapter extends SQLiteOpenHelper {
	
	static{
		try {
			Class.forName("com.singularity.clover.entity.task.Task");
			Class.forName("com.singularity.clover.entity.task.Plan");
			Class.forName("com.singularity.clover.entity.record.Record");
			Class.forName("com.singularity.clover.entity.record.PictureRecord");
			Class.forName("com.singularity.clover.entity.record.VoiceRecord");
			Class.forName("com.singularity.clover.entity.record.TextRecord");
			Class.forName("com.singularity.clover.entity.objective.CheckableObj");
			Class.forName("com.singularity.clover.entity.objective.DurableObj");
			Class.forName("com.singularity.clover.entity.objective.NumericObj");
			Class.forName("com.singularity.clover.entity.wrapper.Scenario");
			Class.forName("com.singularity.clover.entity.notification.Notifier");
			Class.forName("com.singularity.clover.entity.lbs.LBSBundle");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * ����ģʽ��Ψһʵ��
	 */
	private static DBAdapter self = null;
	/**
	 * ���ݿ���
	 */
	private static final String DATABASE_NAME = "SingularityDB.db";
	/**
	 * ���ݿ�汾
	 */
	private static int DATABASE_VERSION = 2;
	
	private Context mContext;

	/**
	 * ���ݿ���������ʼ��������Ψһʵ����
	 * 
	 * @param context
	 *            ����������
	 */
	public static synchronized void initialize(Context context) {
		if (self == null)
			self = new DBAdapter(context);
	}

	/**
	 * ��������������
	 * 
	 * @return Ψһʵ��
	 */
	public static DBAdapter instance() {
		return self;
	}

	/**
	 * ˽�й��캯������ֱֹ�ӹ��졣
	 * 
	 * @param _context
	 *            ����������
	 */
	private DBAdapter(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = _context;
	}

	public void setDBVersion(int newVersion){
		DATABASE_VERSION = newVersion;
	}

	/**
	 * �����ݿⲻ����ʱ�����Խ��������ݿ⡣
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.
	 * 							database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase _db) {
		_db.beginTransaction();
		try {	
			for (Map.Entry<String,Persisable> entry:
							EntityPool.instance().getAllPrototype()){
				_db.execSQL(entry.getValue().getSchema());
			}
			initializeDBData(_db);
			_db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			_db.endTransaction();
		}

	}

	/**
	 * ��ԭ�����ݿ�汾���������Ҫ�����ݿ�汾��һ��ʱ���ã�����ԭ�����ݿ⡣
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.
	 * 							database.sqlite.SQLiteDatabase,
	 *      int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
		_db.beginTransaction();
		try {
			/*for (Map.Entry<String,Persisable> entry:
						EntityPool.instance().getAllPrototype()){
				_db.execSQL("DROP TABLE IF EXISTS "+entry.getKey());
				_db.execSQL(entry.getValue().getSchema());
			}
			initializeDBData(_db);*/
			if(_oldVersion == 1 && _newVersion == 2){
				_db.execSQL(EntityPool.instance().getPrototype(LBSBundle.TAG).getSchema());
			}else{
				
			}
            _db.setTransactionSuccessful();
        } catch (SQLException e) {
        	throw e;
        } finally {
            _db.endTransaction();
        }
	}

	public long insert(String tableName, 
			ContentValues values) throws SQLException {
		long rowId = Global.INVALIDATE_ID;
		rowId = getWritableDatabase().insertOrThrow(tableName, "null", values);
		return rowId;
	}


	public Cursor retrieveById(String tableName, long id) {
		return getReadableDatabase().rawQuery("SELECT * FROM " + tableName + 
				" WHERE _id=?",new String[] { Long.toString(id) });
	}
	
	public Cursor retrieveAll(String tableName,
			String whereClause,String[] whereArgs) {
		if(whereClause != null)
			return getReadableDatabase().rawQuery(
					"SELECT * FROM " + tableName + " "+whereClause, whereArgs);
		else
			return getReadableDatabase().rawQuery(
					"SELECT * FROM " + tableName, null);
	}

	/**
	 * ��ȡ���һ�β�����е���Ŀ��ID
	 * 
	 * @param tableName
	 *            Ҫ��ѯ�ı�����
	 * @return ���һ�β�����Ŀ��ID
	 */
	public long lastInsertId(String tableName) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT seq FROM SQLITE_SEQUENCE WHERE name = ?",
				new String[] { tableName });
		long id = Global.INVALIDATE_ID;
		if(cursor.moveToFirst())
			id = cursor.getLong(0);
		cursor.close();
		return id;
	}
	
	
	public void clearSqliteSequence(){
		deleteEntry("SQLITE_SEQUENCE", "1", null);
	}
	/**
	 * ����������Ŀ�ĸ���������
	 * 
	 * @param tableName
	 *            Ҫ���µ���Ŀ���ڱ�
	 * @param id
	 *            Ҫ������Ŀ��id
	 * @param map
	 *            Ҫ���µ����ݣ��ֶ���-ֵ��ӳ�䣬ʹ��ContentValues�ṹ��
	 */
	public void updateEntry(String tableName, long id, ContentValues values) {
		String[] whereArgs = new String[] { Long.toString(id) };
		int rows = getWritableDatabase().update(tableName, values, "_id =?", whereArgs);
		if(rows == 0)
			throw new NoSuchElementException();
	}

	/**
	 * ɾ����Ŀ����������
	 * 
	 * @param tableName
	 *            Ҫɾ������Ŀ���ڱ�
	 * @param where
	 *            �����ֶ�
	 * @param value
	 *            �ֶ�ֵ
	 */
	public void deleteEntry(String tableName, String where, String[] whereArgs) {
		getWritableDatabase().delete(tableName, where, whereArgs);
	}

	public void addColumn(String tableName,String columnName,String columnType) throws SQLException{
		getWritableDatabase().execSQL("ALTER TABLE "+tableName+
										" ADD COLUMN "+columnName+" "+columnType+";");
	}
	
	public Cursor execQuery(String sql, String[] whereArgs) {
		return getReadableDatabase().rawQuery(sql, whereArgs);
	}
	
	public void execSql(String sql, Object[] bindArgs) {
		getWritableDatabase().execSQL(sql, bindArgs);
	}


	public void beginTransaction() {
		getWritableDatabase().beginTransaction();
	}
	
	public void setTransactionSuccessful() {
		getWritableDatabase().setTransactionSuccessful();
	}
	
	public void endTransaction() {
		getWritableDatabase().endTransaction();
	}
	
	private void initializeDBData(SQLiteDatabase _db){
		String goOut = mContext.getResources().getString(R.string.scenario_go_out);
		String atHome = mContext.getResources().getString(R.string.scenario_at_home);
		String appointment = mContext.getResources().getString(R.string.scenario_appointment);
		_db.execSQL("INSERT OR IGNORE INTO "+Scenario.TAG+" (name,icon_res_id) " +
				"VALUES ('"+goOut+"','"+Color.GREEN + "')");
		_db.execSQL("INSERT OR IGNORE INTO "+Scenario.TAG+" (name,icon_res_id) " +
				"VALUES ('"+atHome+"','"+Color.YELLOW + "')");
		_db.execSQL("INSERT OR IGNORE INTO "+Scenario.TAG+" (name,icon_res_id) " +
				"VALUES ('"+appointment+"','"+Color.BLUE + "')");
	}
}
