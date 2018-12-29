package com.icecold.sleepbandtest.utils;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/17.
 */

public class SingleLinkedList {

    private int size;//链表节点的个数
    private Node head;

    public void addNode(Node node){
        if (size == 0){
            head = new Node();
        }
        Node temp = head;
        while (temp.next != null){
            temp = temp.next;
        }
        temp.next = node;
        size ++;
    }

    public void insertNodeByIndex(int index,Node node){
        //先判断给的位置是否合法
        if (index < 1 || index >= length()+1){
            throw new IndexOutOfBoundsException("输入的位置越界");
        }
        int length = 1;
        Node temp = head;
        while (temp.next != null) {//遍历单链表
            if (index == length++) {//判断是否到达指定的位置。注意这里用的是length++

                node.next = temp.next;//先把下个位置的索引给到新加入的node需要保存的索引中
                temp.next = node;//把插入的上一个的next指向当前这个
                break;
            }
            temp = temp.next;
        }
        size ++;
    }
    public void delNodeByIndex(int index){
        if (index < 1 || index >= length()+1){
            throw new IndexOutOfBoundsException("输入的位置越界");
        }
        int length = 1;
        Node temp = head;
        while (temp.next != null) {
            if (index == length++) {
                temp.next = temp.next.next;
                break;
            }
            temp = temp.next;
        }
        size --;
    }
    public int getData(int index){
        if (index < 0 || index >= length()+1){
            throw new IndexOutOfBoundsException("输入的位置越界");
        }

        int length = 1;
        Node temp = head;
        while (temp.next != null) {
            if (index == length++) {
                return temp.data;
            }
            temp = temp.next;
        }
        return temp.data;
    }

    public int length(){
        return size;
    }
}
