package storage;

import inter.ISloadOperation;
import models.SloadModel;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import parse.Result;

import java.io.Reader;

/**
 * Created by jiangyuan5 on 2017/6/28.
 */
public class MysqlStorage {
    private SloadModel model;

    private static SqlSessionFactory sqlSessionFactory;
    private static Reader reader;

    static{
        try{
            reader    = Resources.getResourceAsReader("dbconfigure.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public  MysqlStorage(SloadModel m) {
        model = m;
    }

    public MysqlStorage(Result r) {
        model = new SloadModel(r);
    }



    public void storage() {
        /*
        SloadModel sload = new SloadModel();
        sload.feedsNum= 1 ;
        sload.availablePos  = 1;
        sload.unreadStatus  =1 ;
        sload.adRatio  =1 ;
        sload.serviceName  = "main_feed";
        sload.year  = 2017;
        sload.month  =6 ;
        sload.day  = 28;
        sload.hour  = 18;
        sload.min = 5;
        */


        SqlSession session = sqlSessionFactory.openSession();

        try {
            //System.out.println("数据库插入");
            ISloadOperation si = session.getMapper(ISloadOperation.class);
            si.addSloadRecord(model);
            session.commit();
        } finally {
            session.close();
        }

    }
}
