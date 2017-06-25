package dataStuct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapSortInteger {
	private HashMap<String, Integer> map;
	private int nowLoc;
	private ArrayList<MapPair> array;
	public MapSortInteger(){
		map=new HashMap<>();
		array=new ArrayList<>();
		nowLoc=-1;
	}
	public void addMapSort(MapSortInteger mapSort){
		mapSort.sort();
		while(mapSort.readRecord()){
			String key=mapSort.getKey();
			int value=mapSort.getValue();
			addString(key,value);
		}
	}
	public void addString(String word,int time){
		if(this.map.containsKey(word)){
			int value=map.get(word);
			map.put(word, value+time);
		}else{
			map.put(word, time);
		}
	}
	public void addString(String word){
		if(this.map.containsKey(word)){
			int value=map.get(word);
			map.put(word, value+1);
		}else{
			map.put(word, 1);
		}
	}
	public void sort(){
		Iterator it=getIter();
		ArrayList<MapPair> array=new ArrayList<>();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry) it.next();
			array.add(new MapPair((String)entry.getKey(),(Integer)entry.getValue()));
		}
		this.array=array;
		this.nowLoc=-1;
	}
	public boolean readRecord(){
		nowLoc++;
		if(nowLoc>=this.array.size()){
			return false;
		}else{
			
			return true;
		}
	}
	public String getKey(){
		return this.array.get(nowLoc).key;
	}
	public int getValue(){
		return this.array.get(nowLoc).value;
	}
	public Map getSortMap(){
		sort();
		return sortMap(this.map);
	}
	public Iterator getIter(){
		Map map=sortMap(this.map);
		return map.entrySet().iterator();
	}
	public static Map sortMap(Map oldmap){
		ArrayList<Map.Entry<String,Integer>> list=new ArrayList<>(oldmap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {  
			  
	            @Override  
	            public int compare(Map.Entry<String, Integer> arg0,  
	                    Map.Entry<java.lang.String, Integer> arg1) {  
	                return arg1.getValue() - arg0.getValue();  
	            }  
	        });
		Map newMap = new LinkedHashMap();  
        for (int i = 0; i < list.size(); i++) {  
            newMap.put(list.get(i).getKey(), list.get(i).getValue());  
        }  
        return newMap;
	}
	class MapPair{
		public String key;
		public int value;
		public MapPair(String key,int value){
			this.key=key;
			this.value=value;
		}
	}
}
