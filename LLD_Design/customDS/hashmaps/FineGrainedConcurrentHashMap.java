package customDS.hashmaps;

import customDS.list.CusDLL;
import customDS.list.Node;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
class AtomicMap <K,V> {
    private volatile ArrayList<CusDLL<K,V>> buckets;
    private AtomicInteger containerSize; //volatile not needed with this

    /// BEST is to create a constructor and swap the atomicMap with that, since atomicMap is volatile, the code will either see the older map  (with older buckets and container size) or newer map (newer bucket and newer size) but not inconsistent data (older bucket, new container size or vice versa).

    AtomicMap (ArrayList<CusDLL<K,V>> buckets, int containerSize){
        this.buckets= buckets;
        this.containerSize = new AtomicInteger(containerSize);
    }

    /// DO NOT do something like this as during read, there is no lock and we can still have weird race conditions.


//    public synchronized void updateMap(ArrayList<CusDLL<K,V>> nBuckets, int nContainerSize){
//        buckets = nBuckets;
//        containerSize.set(nContainerSize);
//    }
}
public class FineGrainedConcurrentHashMap<K,V> {


    private volatile AtomicInteger size;
    private  volatile  AtomicMap<K,V> atomicMap;
    //because we are using it in getIndex and it needs to be consistent
    private int threshold;
    private final double thresholdFactor= 0.75;


    FineGrainedConcurrentHashMap(){
        atomicMap= new AtomicMap<>(new ArrayList<CusDLL<K,V>>(),10);
        size = new AtomicInteger(0);
        threshold= (int)(thresholdFactor * atomicMap.getContainerSize().get());
        for(int i=0; i<10; i++){
            atomicMap.getBuckets().add(new CusDLL<>());
        }
    }

    private int getIndex(K key){
        return (key.hashCode() & Integer.MAX_VALUE)%atomicMap.getContainerSize().get();
    }

    private  int getIndex(K key, int cSize){
        return (key.hashCode() & Integer.MAX_VALUE)%cSize;
    }


    private synchronized void rehash(){

        //double checking to avoid multiple threads rehashing a already rehashed map
        if(size.get()<threshold) return;// some other thread has already rehashed


        int nContainerSize= 2 * atomicMap.getContainerSize().get();
        ArrayList<CusDLL<K,V>> nMap= new ArrayList<CusDLL<K, V>>();
        //TODO: even though rehash is thread locked, put and remove are not. It is possible that you might accidentally end up resurrecting a removed node. Ensure that you block the bucket you are working on.
        for(int i=0; i<nContainerSize; i++){
            nMap.add(new CusDLL<K,V>());
        }
        for(int i=0; i<atomicMap.getContainerSize().get(); i++){
            CusDLL<K,V> currList = atomicMap.getBuckets().get(i);
            Node<K,V> head = currList.getListHead();
            while(head!=null){
                nMap.get(getIndex(head.key,nContainerSize)).add(head.key,head.val);;
                head= head.next;
            }
        }

        this.atomicMap = new AtomicMap<>(nMap,nContainerSize);
        threshold = (int)(thresholdFactor * nContainerSize);

    }

    public void  put(K key, V value){
        /**
         *  put also suffers from same getIndex issue, what if map changes in between, so we need to use snapshots and work with that.
         *  One extra step is, at the end, we verify the atomicMap is same as snapshot (every object has ID in java, hashcode, which quickly tells that). If it's the same, nice, else aqcuire lock again and start process again).
         */

        if(size.get()>=threshold){
            rehash();
        }


        // only puts a lock on that bucket, keeps every other bucket lock free
        /**
         * Two ways to use synchronize
         *  1. to use it as function method , like public void synchronized put (...)
         *      - This would lock put for every write and is slow
         *      - Not recommended, would be coarse grained locking
         *
         *  2. to use an object as argument of Synchronized. (Recommended)
         *      - Every java object has in built lock (monitor/intrinsic lock)
         *      - You put only the object you want to lock in argument of synchronized keyword
         *      - eg: for map, we can only lock the particular DLL we are working on, so if two threads come up with index 0 and 5, they will not wait.
         *      - only when they come with 0 and 0, then one will happen before another due to locking.
         *
         *
         */
        AtomicMap<K,V> snapshot;
        boolean hasAdded = false;
        do{
            snapshot = atomicMap;
            hasAdded =false;
            int ind = getIndex(key, snapshot.getContainerSize().get());
            synchronized (snapshot.getBuckets().get(ind)){
                if(snapshot.getBuckets().get(ind).find(key)==null){
                    snapshot.getBuckets().get(ind).add(key,value);
                    hasAdded = true;
                }
                else {
                    snapshot.getBuckets().get(ind).update(key,value);
                }
            }
        } while (atomicMap!=snapshot);

        if (hasAdded)
            size.incrementAndGet();
    }
    public V get(K key){

        /** Snapshot
         * we save snapshot when we enter the get so that even if map changes in between, we have older snapshot and are able to provide accurate result.
         *
         * Use only snapshot for rest of the operations
         */

        AtomicMap<K,V> snapshot = atomicMap;

        int ind= getIndex(key,snapshot.getContainerSize().get());
        /** Why we need to worry about locks while Reading (common mistake to forget)
         * Even though get in itself in not modifying anything, it might happen that a put or remove is happening, and you come across an empty pointer/null pointer or invalid state.
         * Remember : compiler DOES NOT follow exact lines of steps, it can REORDER
         *      - It might link the next pointer before the newNode's data is actually flushed to
         *      main memory. If that happened, a reader would follow the pointer and find a
         *      "zombie" node with null data.
         *
         *  We use Volatile to force compiler to do things in order for that variable (update nodes).
         */


        /** Two ways to ensure consistency
         *  Now there are two ways,
         *  1. we lock bucket for reading (yes, the write locks doesn't work until we also let read know there is a lock)
         *  2. we make changes in Node and make the data pointer volatile.  (This is recommended since it provides lock-free mechanism)
         *  - when user does read, you either get old data or new data, never null experience.
         *  - eventual consistency (weak consistency) -> acceptable due to fast lock-free
         */

         /**
         *  Is there a "Nanosecond" gap?
         * There is a gap in time, but not a gap in logic:
         *
         * During the write: Readers continue to see the old, perfectly valid version of the list.
         *
         * Immediately after the write: Readers see the new, perfectly valid version of the list.
         *
         * The list is never in an "invalid" state where a pointer is dangling or broken; it simply transitions from "Old Version" to "New Version" instantaneously from the perspective of any other thread.
         */


        if(snapshot.getBuckets().get(ind).find(key)==null)
            return null;
        else return snapshot.getBuckets().get(ind).find(key).val;
    }

    public boolean remove(K key){
        AtomicMap<K,V> snapshot;
        boolean hasFound=false;
        do{
            hasFound=false;
            snapshot= atomicMap;
            int ind= getIndex(key, snapshot.getContainerSize().get());

            synchronized (snapshot.getBuckets().get(ind)) {
                if (snapshot.getBuckets().get(ind).find(key) != null) {
                    snapshot.getBuckets().get(ind).remove(key);
                    hasFound = true;
                }
            }
        }while(snapshot!=atomicMap);

        if(hasFound)
            size.decrementAndGet();
        return  hasFound;
    }
}
