package service;


import domain.Item;

public interface ItemService {

    Item insertItem(Item item);
    Item getItemById(String id);
    Item updateItemById(String id, Item item);
    void deleteItemById(String id);


}
