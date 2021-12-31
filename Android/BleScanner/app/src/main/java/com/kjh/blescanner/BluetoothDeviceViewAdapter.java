package com.kjh.blescanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kjh.blescanner.domain.BluetoothDeviceData;

import java.util.ArrayList;

public class BluetoothDeviceViewAdapter extends RecyclerView.Adapter<BluetoothDeviceViewAdapter.ViewHolder> {

    private ArrayList<BluetoothDeviceData> list = null ;

    public void clear() {
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        ArrayList<BluetoothDeviceData> list = new ArrayList<>();
        list.add(new BluetoothDeviceData("이름", "MAC", null));
        this.list = list;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMAC, tvUUID;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            tvName = itemView.findViewById(R.id.tvName);
            tvMAC = itemView.findViewById(R.id.tvMAC);
            tvUUID = itemView.findViewById(R.id.tvUUID);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    BluetoothDeviceViewAdapter() {
        clear();
    }


    public void addDevice(BluetoothDevice device) {

        String name = device.getName();
        String mac = device.getAddress();
        String uuids[] = null;
        ParcelUuid parcelUuids[] = device.getUuids();

        Log.d("parcelUuids", parcelUuids+"");

        if (parcelUuids != null) {
            uuids = new String[parcelUuids.length];

            for (int i=0; i<parcelUuids.length; i++) {
                uuids[i] = parcelUuids[i].toString();
            }
        }

        BluetoothDeviceData data = new BluetoothDeviceData(name, mac, uuids);
        list.add(data);
        Log.d("deviceList", list+"");
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public BluetoothDeviceViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.device_item, parent, false) ;
        BluetoothDeviceViewAdapter.ViewHolder vh = new BluetoothDeviceViewAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(BluetoothDeviceViewAdapter.ViewHolder holder, int position) {
        BluetoothDeviceData bluetoothDeviceData = list.get(position);

        holder.tvName.setText(bluetoothDeviceData.getName());
        holder.tvMAC.setText(bluetoothDeviceData.getMac());
        if (bluetoothDeviceData.getUuids() != null) {
            holder.tvUUID.setText(bluetoothDeviceData.getUuids()[0]);
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return list.size();
    }
}