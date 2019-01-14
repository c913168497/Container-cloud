package org.application.common.listener;


/**
 * 事件监听器
 * @param <T> 数据源对象
 * @param <O> 输出对象
 */
public interface EventListener<T,O> {
    /**
     * 事件触发之前发生
     * @param source
     */
//    public void before(T source) ;
    /**
     * 正在处理
     * @param output 输出内容
     * @param  source 事件源对象
     *
     */
    public void onEvent(T source, String output);

    /**
     * 完成事件
     * @param source 事件源对象
     */
    public void onFinish(T source, O out);

    /**
     * 失败事件
     * @param source 事件源对象
     * @param error 错误信息
     */
    public  void onError(T source, String error) ;

}
