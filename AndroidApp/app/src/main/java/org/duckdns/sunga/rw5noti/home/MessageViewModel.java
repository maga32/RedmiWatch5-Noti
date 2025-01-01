package org.duckdns.sunga.rw5noti.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MessageViewModel extends ViewModel {

    private final MutableLiveData<String> _text = new MutableLiveData<>();

    public MessageViewModel() {
        _text.setValue("This is dashboard Fragment");
    }

    public LiveData<String> getText() {
        return _text;
    }
}
