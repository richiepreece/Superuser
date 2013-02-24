package com.koushikdutta.superuser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.koushikdutta.superuser.util.Settings;
import com.koushikdutta.widgets.BetterListFragment;
import com.koushikdutta.widgets.ListItem;

public class SettingsFragment extends BetterListFragment {
    @Override
    protected int getListFragmentResource() {
        return R.layout.settings;
    }
    
    static final int containerId = 100001;
    public static class MyPinFragment extends DialogFragment {
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FrameLayout ret =  new FrameLayout(getActivity());
            ret.setId(containerId);
            return ret;
        };
        
        private int title;
        public void setTitle(int title) {
            this.title = title;
        }

        Dialog d;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            d = super.onCreateDialog(savedInstanceState);
            d.setTitle(title);
            PinFragment pf = new PinFragment() {
                @Override
                public void onCancel() {
                    super.onCancel();
                    d.dismiss();
                }
                
                @Override
                public void onEnter(String password) {
                    super.onEnter(password);
                    MyPinFragment.this.onEnter(password);
                }
            };
            getChildFragmentManager().beginTransaction().add(containerId, pf).commit();

            return d;
        }
        
        public void onEnter(String password) {
            d.dismiss();
        }
    };
    
    void confirmPin(final String pin) {
        MyPinFragment p = new MyPinFragment() {
            @Override
            public void onEnter(String password) {
                super.onEnter(password);
                if (pin.equals(password)) {
                    Settings.setPin(getActivity(), password);
                    if (password != null && password.length() > 0)
                        Toast.makeText(getActivity(), getString(R.string.pin_set), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(), getString(R.string.pin_mismatch), Toast.LENGTH_SHORT).show();
            }
        };
        p.setTitle(R.string.confirm_pin);
        p.show(getFragmentManager(), "pin");
    }
    
    void setPin() {
        MyPinFragment p = new MyPinFragment() {
            @Override
            public void onEnter(String password) {
                super.onEnter(password);
                confirmPin(password);
            }
        };
        p.setTitle(R.string.enter_new_pin);
        p.show(getFragmentManager(), "pin");
    }

    void checkPin() {
        if (Settings.isPinProtected(getActivity())) {
            MyPinFragment p = new MyPinFragment() {
                @Override
                public void onEnter(String password) {
                    if (Settings.checkPin(getActivity(), password)) {
                        super.onEnter(password);
                        setPin();
                        return;
                    }
                    Toast.makeText(getActivity(), getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }
            };
            p.setTitle(R.string.enter_pin);
            p.show(getFragmentManager(), "pin");
        }
        else {
            setPin();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState, View view) {
        super.onCreate(savedInstanceState, view);
        
        addItem(R.string.settings, new ListItem(this, R.string.logging, R.string.logging_summary, R.drawable.ic_logging) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                Settings.setLogging(getActivity(), getChecked());
            }
        })
        .setCheckboxVisible(true)
        .setChecked(Settings.getLogging(getActivity()));
        
        addItem(R.string.settings, new ListItem(this, R.string.pin_protection, R.string.pin_protection_summary, R.drawable.ic_protected) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                checkPin();
            }            
        });
        
        addItem(R.string.settings, new ListItem(this, getString(R.string.request_timeout), getString(R.string.request_timeout_summary, Settings.getRequestTimeout(getActivity())), R.drawable.ic_timeout) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.request_timeout);
                String[] seconds = new String[3];
                for (int i = 0; i < seconds.length; i++) {
                    seconds[i] = getString(R.string.number_seconds, (i + 1) * 10);
                }
                builder.setItems(seconds, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.setTimeout(getActivity(), (which + 1) * 10);
                        setSummary(getString(R.string.request_timeout_summary, Settings.getRequestTimeout(getActivity())));
                    }
                });
                builder.create().show();
            }
        });

        
        addItem(R.string.settings, new ListItem(this, R.string.notifications, R.string.notifications_summary, R.drawable.ic_notifications) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.notifications);
                String[] items = new String[] { getString(R.string.none), getString(R.string.toast), getString(R.string.notification) };
                builder.setItems(items, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        case 0:
                            Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_NONE);
                            break;
                        case 2:
                            Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_NOTIFICATION);
                            break;
                        default:
                            Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_TOAST);
                            break;
                        }
                    }
                });
                builder.create().show();
            }
        });
    }
}