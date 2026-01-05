package customDS.list;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CusDLL<K, V>{

    @Setter(AccessLevel.PRIVATE)
    private int size=0;
    private Node<K,V> listHead;
    private Node<K,V> listTail;
    public Node<K,V> find(K key){
        Node<K,V> curr = listHead;
        while(curr!=null){
            if(curr.key.equals(key))
                return  curr;
            curr=curr.next;
        }
        return null;
    }
    public void pushNode(Node<K,V> node){
        node.next= null;
        node.prev= null;
        if(listHead==null){
            listHead= node;
        }else {
            listTail.next=node;
            node.prev = listTail;
        }
        listTail= node;
        size++;
    }
    public void add(K key, V val){
        Node<K,V> node = new Node<K,V>(key,val);
        pushNode(node);
    }

    public void remove(K key){
        Node<K,V> node = find(key);
        if(node==null){
            throw new RuntimeException("The Key you are trying to delete doesn't exist");
        }
        if(node.prev!=null) node.prev.next= node.next;
        else listHead= node.next;

        if(node.next!=null) node.next.prev= node.prev;
        else listTail=  node.prev;
        size--;

    }

    public void update(K key, V newValue){
        Node<K,V> node = find(key);
        if(node==null){
            throw new RuntimeException("The Key you are trying to update doesn't exist");
        }
        node.val = newValue;
    }
    public void print(){
        Node<K,V> curr = listHead;
        while(curr!= null){
            System.out.print(curr.key + " : " + curr.val + "->");
            curr= curr.next;
        }
        System.out.print("NULL\n");
    }

}