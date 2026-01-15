package customDS.list;

public class Node<K,V>{
    public final K key; // because we never want to change key value
    public V val;
    public volatile Node<K,V> next;
    public volatile Node<K,V> prev;

    public Node(K key, V val){
        this.key = key;
        this.val= val;
        this.next=null;
        this.prev= null;
    }
}