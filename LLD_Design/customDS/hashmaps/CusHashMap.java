package customDS.hashmaps;

import customDS.list.CusDLL;
import customDS.list.Node;
import java.util.ArrayList;
import java.util.List;

public class CusHashMap<K,V> {

    private int size;
    public int containerSize;
    private  int threshold;
    private List<CusDLL<K,V>> mapContainer;
    public CusHashMap(){
        this.size=0;
        this.containerSize=10;
        this.threshold= getThreshold(containerSize);
        this.mapContainer= new ArrayList<>(containerSize);
        for(int i=0; i<containerSize; i++){
            mapContainer.add(new CusDLL<>());
        }
    }
    private int getContainerIndex(K key){
        return Math.abs(key.hashCode())%containerSize;
    }

    private int getThreshold(int containerSize){
        double lbFactor = 0.75;
        return (int) (containerSize* lbFactor);
    }

    private void reHash(){
        int nContainerSize= containerSize *2;
        List<CusDLL<K,V>> nMapContainer = new ArrayList<>(nContainerSize);
        for(int i=0; i<nContainerSize; i++){
            nMapContainer.add(i, new CusDLL<>());
        }
        for(int i=0; i<containerSize; i++){
            CusDLL<K,V> list = mapContainer.get(i);
            Node<K,V> curr= list.getListHead();
            while (curr != null) {
                Node<K,V> nextTemp = curr.next;
                int nIndex= Math.abs(curr.key.hashCode())%nContainerSize;
                nMapContainer.get(nIndex).pushNode(curr);
                curr= nextTemp;
            }

        }
        mapContainer=nMapContainer;
        containerSize= nContainerSize;
        threshold = getThreshold(containerSize);
    }


    public void add(K key, V val){
        if(size>threshold){
            reHash();
        }
        int ind = getContainerIndex(key);
        if(mapContainer.get(ind).find(key)==null) {
            mapContainer.get(ind).add(key, val);
            size++;
        }else{
            mapContainer.get(ind).update(key,val);
        }

    }
    public V get(K key){
        int ind = getContainerIndex(key);
        Node<K,V> node= mapContainer.get(ind).find(key);
        if(node==null) return  null;
        return  node.val;

    }
    public void remove(K key){
        int ind = getContainerIndex(key);
        try{
            mapContainer.get(ind).remove(key);
            size--;
        }catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }


    }
    public void update(K key, V newVal){
        int ind = getContainerIndex(key);
        try{
            mapContainer.get(ind).update(key, newVal);
        }catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }
}
