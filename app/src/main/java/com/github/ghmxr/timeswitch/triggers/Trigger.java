package com.github.ghmxr.timeswitch.triggers;

/**
 * ��ȡ���Ա�������������ǰ���ȵ���cancel��ȡ��������
 */
public interface Trigger{
     /**
      * ���ô������������ظ�����
      */
     void activate();

     /**
      * �رմ������������ظ�����
      */
     void cancel();
}
