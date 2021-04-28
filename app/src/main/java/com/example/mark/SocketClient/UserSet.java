package com.example.mark.SocketClient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class UserSet<V>
{
	//建立一個安執行緒安全的HashMap
	public Set<V> set= Collections.synchronizedSet(new HashSet<V>());

	//根據Value來刪除指定項目
	public synchronized void remove(Object value)
	{
		set.remove(value);
	}

	//獲取所有value組成的Set集合
	public synchronized Set<V> valueSet()
	{
		return set;
	}

	//實作put()方法，該方法不允許value重複
	public synchronized void put(Object value)
	{
		set.add((V) value);
	}
}
