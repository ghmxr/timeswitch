# timeswitch(部件触发器/Widget Trigger)
*****************************
 这是一个通过设置一些触发条件来自动触发并执行一些任务的Android平台的APP。比如定时静音、打开关闭无线网络、切换铃声，或者检测到插入耳机时自动运行音乐播放器等等。用户可自定义搭配触发器、例外规则以及要执行的操作。应用基本的运行模式为触发器-例外判断-执行操作；本APP触发器的主要实现类为BroadcastReceiver，搭配AlarmManager和Java TimerTask来实现定时触发执行操作。应用的核心类为TaskItem，内部包含了触发器实例以及任务的所有参数；数据存储使用SQLite，应用运行时会将数据库表读出来并加载成一个List对象来存储。每个数据库行存储了单个任务项的所有参数，每个数据库表对应一个可以切换的列表。
 
 ************
 酷安市场:<a href="https://www.coolapk.com/apk/com.github.ghmxr.timeswitch">  https://www.coolapk.com/apk/com.github.ghmxr.timeswitch</a>
 ************
 
 <div align="center">
	<img src="https://github.com/ghmxr/timeswitch/raw/master/preview/timeswitch_1.png" alt="avator" title="" width="180"/>
	<img src="https://github.com/ghmxr/timeswitch/raw/master/preview/timeswitch_2.png" alt="avator" title="" width="180"/>
	<img src="https://github.com/ghmxr/timeswitch/raw/master/preview/timeswitch_3.png" alt="avator" title="" width="180"/>
	<img src="https://github.com/ghmxr/timeswitch/raw/master/preview/timeswitch_4.png" alt="avator" title="" width="180"/>
	<img src="https://github.com/ghmxr/timeswitch/raw/master/preview/timeswitch_5.png" alt="avator" title="" width="180"/>
 </div>
 
 

