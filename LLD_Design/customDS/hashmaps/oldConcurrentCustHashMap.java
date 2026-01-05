package customDS.hashmaps;

public class oldConcurrentCustHashMap<K,V> extends CusHashMap<K,V> {

    //very slow, it allows only one thread at a time to read/write
    public synchronized void add(K key, V val){
        super.add(key, val);
    }
    public synchronized  void remove(K key){
        super.remove(key);
    }
}
