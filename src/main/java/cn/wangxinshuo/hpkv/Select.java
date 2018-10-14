package cn.wangxinshuo.hpkv;

import cn.wangxinshuo.hpkv.cache.FileCache;
import cn.wangxinshuo.hpkv.file.FileResources;
import cn.wangxinshuo.hpkv.util.ByteArrayToUnsignedLong;
import com.alibabacloud.polar_race.engine.common.exceptions.EngineException;
import com.alibabacloud.polar_race.engine.common.exceptions.RetCodeEnum;
import com.google.common.primitives.UnsignedLong;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author wszgr
 */
public class Select {
    private FileResources resources;
    /**
     * 利用LRU缓存淘汰算法来进行缓存
     * LRU:Least Recently Used
     */
    private ArrayList<FileCache> fileCaches;
    private final int FILE_CACHE_LEN = 128;
    private HashMap<UnsignedLong, byte[]> map;

    private Select(FileResources resources) {
        this.resources = resources;
        fileCaches = new ArrayList<FileCache>(FILE_CACHE_LEN);
    }

    public Select(FileResources resources, HashMap<UnsignedLong, byte[]> map) {
        this(resources);
        this.map = map;
    }

    public byte[] get(byte[] inKey) throws EngineException {
        UnsignedLong key = ByteArrayToUnsignedLong.getKey(inKey);
        // 去MemTable中查找，由于map初始化的时候log文件就已经写入，
        // 所以不需要再去Log文件里面查找
        if (map.containsKey(key)) {
            System.out.println("从map中获得查询的数据！");
            return map.get(key);
        }
        // 从文件缓存中查找
        for (FileCache aLruList : fileCaches) {
            HashMap<UnsignedLong, byte[]> map = aLruList.getData();
            if (map.containsKey(key)) {
                byte[] value = map.get(key);
                fileCaches.remove(aLruList);
                fileCaches.add(0, aLruList);
                System.out.println("从文件缓存中获得查询的数据！");
                return value;
            }
        }
        // 去文件中查找
        for (int i = 0; i < resources.getNumberOfFiles(); i++) {
            try {
                InputStream stream = new FileInputStream(resources.getReadSources(i));
                if (stream.available() != 0) {
                    // 读取序列化之后的byte
                    byte[] serializedArray = new byte[stream.available()];
                    final int read = stream.read(serializedArray);
                    // 反序列化
                    HashMap<UnsignedLong, byte[]> map =
                            SerializationUtils.deserialize(serializedArray);
                    if (map.containsKey(key)) {
                        if (map.size() == FILE_CACHE_LEN) {
                            fileCaches.remove(FILE_CACHE_LEN);
                        }
                        System.out.println("从序列化后的Map中获得查询的数据！");
                        fileCaches.add(0, new FileCache(i, map));
                        return map.get(key);
                    }
                }
            } catch (IOException e) {
                throw new EngineException(RetCodeEnum.IO_ERROR, "IO_ERROR");
            }
        }
        throw new EngineException(RetCodeEnum.NOT_FOUND, "NOT_FOUND");
    }
}
