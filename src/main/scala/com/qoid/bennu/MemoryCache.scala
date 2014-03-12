package com.qoid.bennu

import m3.LockFreeMap

class MemoryCache[K,V] {
  private val map = new LockFreeMap[K, V]

  def put(key: K, value: V): Unit = map.put(key, value)
  def get(key: K): Option[V] = map.get(key)
  def remove(key: K): Unit = map.remove(key)
  override def toString: String = "MemoryCache(" + map.toString + ")"
}

class MemoryListCache[K,V] {
  private val map = new LockFreeMap[K, List[V]]

  def put(key: K, value: V): Unit = map.put(key, value :: get(key))
  def get(key: K): List[V] = map.getOrElse(key, Nil)
  def remove(key: K, value: V): Unit = map.put(key, get(key).filterNot(value.equals))
  override def toString: String = "MemoryListCache(" + map.toString + ")"
}
