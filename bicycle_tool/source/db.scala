package bycicle_tool

import org.iq80.leveldb.{DB, _}
import org.fusesource.leveldbjni.JniDBFactory._
import java.io._

import utils._

//≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈

object db_utils {

    private lazy val _db : DB = {
        sConfig.onCloseCallbacks += {() => if (_db != null) _db.close() }
        openDb()
    }

    //----------------------------------------------------------------------

    def openDb() : DB = {
        val options = new Options()
        options.createIfMissing(true)

        val db = factory.open(
            new File(sConfig.sDBDirPath),
            options
        )

        if (db == null) {
            exitWithError("Con't open build database'")
        }

        return db
    }

    //----------------------------------------------------------------------

    def checkModified(path : String) : Boolean = {
        val file = new File(path)
        if (!file.exists()) return true

        val fileModifiedKey = file.getCanonicalPath() + "_lastModified"
        val lastModifiedBytes = _db.get(
            bytes(fileModifiedKey),
            new ReadOptions()
        )
        if (lastModifiedBytes == null ) {
            return true
        } else {
            val lmodDB = new String(lastModifiedBytes).toLong
            val modCur = recursiveLatestModified(file)

            System.out.println(s"$path saved last modify: $lmodDB, current modify date: $modCur")

            return modCur > lmodDB
        }
    }

    //----------------------------------------------------------------------
}