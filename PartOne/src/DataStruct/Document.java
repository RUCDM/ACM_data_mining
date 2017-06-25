/**
 * 
 */
package DataStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * @author wenhui zhang

 */
public class Document {
	public int[] docWords;
	
	public Document(ArrayList<String> words,HashMap<String, Integer> termToIndexMap, 
			ArrayList<String> indexToTermMap, HashMap<String, Integer> termCountMap){ 
		this.docWords = new int[words.size()];
		for(int i = 0; i < words.size(); i++){
			String word = words.get(i);
			if(!termToIndexMap.containsKey(word)){
				int newIndex = termToIndexMap.size();
				termToIndexMap.put(word, newIndex);
				indexToTermMap.add(word);
				termCountMap.put(word, new Integer(1));
				docWords[i] = newIndex;
			} else {
				docWords[i] = termToIndexMap.get(word);
				termCountMap.put(word, termCountMap.get(word) + 1);
			}
		}
	}
	
}
