package com.kjh.blescanner;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.kjh.blescanner.domain.BluetoothDeviceData;

import java.util.ArrayList;

public class BluetoothDeviceViewAdapter extends RecyclerView.Adapter<BluetoothDeviceViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BluetoothDeviceData> itemList = null ;
    private ArrayList<BluetoothDevice> deviceList = null ;

    // 생성자에서 데이터 리스트 객체를 전달받음.
    BluetoothDeviceViewAdapter(Context context) {
        this.context = context;
        clear();
    }

    public void clear() {
        ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
        this.deviceList = deviceList;
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        ArrayList<BluetoothDeviceData> itemList = new ArrayList<>();
        itemList.add(new BluetoothDeviceData("이름", "MAC", null));
        this.itemList = itemList;
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

            itemView.setOnClickListener(listener->{
                String uuid = getUuid();
                tvUUID.setText(uuid);
            });
        }

        private String getUuid() {
            int pos = getAdapterPosition();
            if (pos > 0) {
                BluetoothDevice device = deviceList.get(pos);

                Toast.makeText(context, "선택한 디바이스 이름: " +
                        device.getName() + "\nMAC: " + device.getAddress(),
                        Toast.LENGTH_SHORT).show();

//                BluetoothGatt bluetoothGatt = device.connectGatt(context, false, gattCallback);
            }
            return "";
        }
    }


    public boolean addDevice(BluetoothDevice device) {

        String name = device.getName();
        String mac = device.getAddress();
        String uuids[] = null;
        ParcelUuid parcelUuids[] = device.getUuids();

//        Log.d("parcelUuids", parcelUuids+"");

        if (parcelUuids != null) {
            uuids = new String[parcelUuids.length];

            for (int i=0; i<parcelUuids.length; i++) {
                uuids[i] = parcelUuids[i].toString();
            }
        }

        if (name == null || name.isEmpty()) {
            name = "알 수 없음";
        }

        if (uuids == null) {
            uuids = new String[]{"클릭하면 연결해서 UUID 출력"};
        }

        BluetoothDeviceData data = new BluetoothDeviceData(name, mac, uuids);
        boolean duplicated = false;

        for (BluetoothDeviceData prevData : itemList) {
            if (data.getMac().equals(prevData.getMac())) {
                duplicated = true;
                break;
            }
        }

        if (!duplicated) {
            itemList.add(data);
            deviceList.add(device);
            Log.d("deviceList", itemList +"");
        }
        return !duplicated;
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
        BluetoothDeviceData bluetoothDeviceData = itemList.get(position);

        holder.tvName.setText(bluetoothDeviceData.getName());
        holder.tvMAC.setText(bluetoothDeviceData.getMac());
        if (bluetoothDeviceData.getUuids() != null) {
            holder.tvUUID.setText(bluetoothDeviceData.getUuids()[0]);
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}