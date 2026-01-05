package customDS.list;

public class Node<K,V>{
    public K key;
    public V val;
    public Node<K,V> next;
    public Node<K,V> prev;

    public Node(K key, V val){
        this.key = key;
        this.val= val;
        this.next=null;
        this.prev= null;
    }
}