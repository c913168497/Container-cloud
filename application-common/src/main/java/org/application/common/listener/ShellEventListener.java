package org.application.common.listener;


import lombok.NoArgsConstructor;

import java.util.Optional;


@NoArgsConstructor
public class ShellEventListener<P,O>{

    private P  p;

    StringBuffer content = new StringBuffer();

    private EventListener<P,O> listener ;

    public ShellEventListener(P  p, EventListener<P,O> listener) {
        this.p = p ;
        this.listener = listener;
    }

    public void onError(String output) {
        if(Optional.ofNullable(listener).isPresent())
        listener.onError(p, output);
    }


    public void output(String output) {
        if(Optional.ofNullable(listener).isPresent())
        listener.onEvent(p, output);
        content.append(output +"\r\n");
    }

    public String getContent() {
        return content.toString();
    }

}
