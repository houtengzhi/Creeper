package com.cloud.creeper.support.livedata;

/**
 * Created by cloud on 2022/8/4.
 * See https://github.com/KunMinX/UnPeek-LiveData
 */
public class UnPeekLiveData<T> extends ProtectedUnPeekLiveData<T> {

    public UnPeekLiveData(T value) {
        super(value);
    }

    public UnPeekLiveData() {
        super();
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    public static class Builder<T> {

        /**
         * 是否允许传入 null value
         */
        private boolean isAllowNullValue;

        public Builder<T> setAllowNullValue(boolean allowNullValue) {
            this.isAllowNullValue = allowNullValue;
            return this;
        }

        public UnPeekLiveData<T> create() {
            UnPeekLiveData<T> liveData = new UnPeekLiveData<>();
            liveData.isAllowNullValue = this.isAllowNullValue;
            return liveData;
        }
    }
}
