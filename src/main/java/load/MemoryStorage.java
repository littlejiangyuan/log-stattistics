package load;

import parse.Result;
import storage.MysqlStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Created by jiangyuan5 on 2017/6/29.
 */
public class MemoryStorage {
    private final Map<String, Result> map = new HashMap<String, Result>();

    public void add( Result s) {
        String key = s.getKey();
        Result ms = map.get(key);
        if(ms != null) {
            s.add(ms);
        }

        map.put(key, s);
    }

    public void flushToDb() {
        for (Entry<String, Result> entry : map.entrySet()) {
            Result s = entry.getValue();
            MysqlStorage ms = new MysqlStorage(s);
            ms.storage();
        }

        //清空
        map.clear();
    }

    public int getItemsCount() {
        return map.size();
    }
}
