package service;

import domain.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class ItemServiceImplMap implements ItemService {


    //The config parameters for the connection
    private static final String HOST = "localhost";
    private static final int PORT_ONE = 9200;
    private static final int PORT_TWO = 9201;
    private static final String SCHEME = "http";

    private static RestHighLevelClient restHighLevelClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Implemented Singleton pattern here
     * so that there is just one connection at a time.
     * @return RestHighLevelClient
     */
    private static synchronized RestHighLevelClient makeConnection() {

        if(restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, SCHEME),
                            new HttpHost(HOST, PORT_TWO, SCHEME)));
        }

        return restHighLevelClient;
    }

    private static synchronized void closeConnection() throws IOException {
        restHighLevelClient.close();
        restHighLevelClient = null;
    }


    private static final String INDEX = "itemdata";
    private static final String TYPE = "item";

    public Item insertItem(Item item) {
        makeConnection();
        if (!validarSite(item)){
            return null;
        }
        item.setId(UUID.randomUUID().toString());
        IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, item.getId())
                .source(item.toDataMap());
        try {
            IndexResponse response = restHighLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            closeConnection();
        } catch(ElasticsearchException e) {
            e.getDetailedMessage();
        } catch (IOException ex){
            ex.getLocalizedMessage();
        }
        return item;

    }

    public Item getItemById(String id) {
        makeConnection();
        GetRequest getItemRequest = new GetRequest(INDEX, TYPE, id);
        GetResponse getResponse = null;
        try {
            getResponse = restHighLevelClient.get(getItemRequest,RequestOptions.DEFAULT);
            closeConnection();
        } catch (IOException e){
            e.getLocalizedMessage();
        }
        return getResponse != null ?
                objectMapper.convertValue(getResponse.getSourceAsMap(), Item.class) : null;    }

    public Item updateItemById(String id, Item item) {
        makeConnection();
        UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, id)
                .fetchSource(true);    // Fetch Object after its update
        try {
            String itemJson = objectMapper.writeValueAsString(item);
            updateRequest.doc(itemJson, XContentType.JSON);
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            closeConnection();
            return objectMapper.convertValue(updateResponse.getGetResult().sourceAsMap(), Item.class);
        }catch (JsonProcessingException e){
            e.getMessage();
        } catch (IOException e){
            e.getLocalizedMessage();
        }
        System.out.println("Unable to update item");
        return null;
    }

    public void deleteItemById(String id) {
        makeConnection();
        DeleteRequest deleteRequest = new DeleteRequest(INDEX, TYPE, id);
        try {
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            closeConnection();
        } catch (IOException e){
            e.getLocalizedMessage();
        }
    }

    private boolean validarSite(Item item){
        try {
            URL url = new URL("http://api.mercadolibre.com/sites/" + item.getSiteId());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            return connection.getResponseCode() != 200;
        }  catch (MalformedURLException m) {
            System.out.println("MalformedURLException: " + m);
        }
        catch (IOException e) {
            System.out.println("IOException: " + e);
        }
        return true;
    }
}
