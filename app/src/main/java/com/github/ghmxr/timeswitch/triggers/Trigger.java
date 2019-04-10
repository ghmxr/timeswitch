package com.github.ghmxr.timeswitch.triggers;

/**
 * 在取消对本触发器的引用前须先调用cancel来取消触发器
 */
public interface Trigger{
     /**
      * 启用触发器，允许重复调用
      */
     void activate();

     /**
      * 关闭触发器，允许重复调用
      */
     void cancel();
}
