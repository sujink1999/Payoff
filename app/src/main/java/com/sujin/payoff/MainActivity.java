package com.sujin.payoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.internal.tls.BasicTrustRootIndex;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ConnectionLifecycleCallback mConnectionLifecycleCallback;
    EndpointDiscoveryCallback endpointDiscoveryCallback;
    PayloadCallback payloadCallback;
    String myName = "name";
    String connected;
    boolean isSender = false;
    ImageView send;
    Gson gsonConverter = new Gson();
    List<String> endpoints = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> availableBalance = new ArrayList<>();
    ListView list;
    PeopleAdapter peopleAdapter;
    Retrofit retrofit;
    Gson gson;
    com.sujin.payoff.Api service;
    CreateTransaction resp = null;
    int jValue = -1;
    TextView btc;
    EditText enter_amount;
    CardView amountCard;
    boolean transSuccess =false;
    int totalBalance= 0;
    LinearLayout processing;
    TextView sendtext;
    String gendpoint;
    boolean connectSuccess = false;
    boolean withoutInternet = false;
    TextView textName;

    //LOCAL
    Initialization customerDetails;

    String id = "id1";
    /* String id = "id2";
    * String id = "id3";
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(Api.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }

        myName = getIntent().getStringExtra("name");
        initialize();
        findViewByIds();
        setOnClickListeners();

        initializeCustomerDetails(myName);

    }


    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        id, "com.sujin.payoff", mConnectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Advertising Started!", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                );
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery("com.sujin.payoff", endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Discovery Started!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void stopDiscovery() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        Toast.makeText(this, "Discovery Stopped.", Toast.LENGTH_SHORT).show();

    }

    private void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
        Toast.makeText(this, "Advertising Stopped.", Toast.LENGTH_SHORT).show();

    }



    private void initialize()
    {
        endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {

                endpoints.add(s);
                names.add(discoveredEndpointInfo.getEndpointName());
                peopleAdapter.notifyDataSetChanged();

            }

            @Override
            public void onEndpointLost(@NonNull String s) {

            }
        };

        mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
                Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(s, payloadCallback);
            }

            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                // We're connected! Can now start sending and receiving data.
                    if(resp!=null) {
                        String json = gsonConverter.toJson(resp);

                        if(s.equals(gendpoint))
                        {
                            connectSuccess = true;
                        }

                        Payload bytesPayload = Payload.fromBytes(json.getBytes());
                        if (jValue != -1) {
                            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoints.get(jValue), bytesPayload);

                        }
                        if(transSuccess)
                        {
                            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(gendpoint, bytesPayload);
                            processing.setVisibility(View.GONE);
                            amountCard.setVisibility(View.VISIBLE);
                            send.setVisibility(View.VISIBLE);
                            sendtext.setVisibility(View.VISIBLE);
                            enter_amount.setText("");

                        }

                    }
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                // The connection was rejected by one or both sides.
                Toast.makeText(MainActivity.this, "Connection Rejected", Toast.LENGTH_SHORT).show();
                break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                // The connection broke before it was able to be accepted.
                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
            }

            @Override
            public void onDisconnected(@NonNull String s) {

            }
        };

        payloadCallback = new PayloadCallback() {

            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                String payloadString = new String(payload.asBytes());
                Toast.makeText(MainActivity.this, payloadString, Toast.LENGTH_SHORT).show();
                CreateTransaction transaction = gsonConverter.fromJson(payloadString,CreateTransaction.class);
                // check for k2 encryption
                if(id.equals(transaction.getTo()))
                {
                    Toast.makeText(MainActivity.this, "Money Received", Toast.LENGTH_SHORT).show();
                    for(int i =0 ; i<transaction.getAmount().size();i++)
                    {
                        availableBalance.add(transaction.getAmount().get(i));
                    }
                    totalBalance+=transaction.getValue();
                    btc.setText(Integer.toString(totalBalance)+" BTC");
                    processing.setVisibility(View.GONE);
                    amountCard.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                    sendtext.setVisibility(View.VISIBLE);
                    //increase the wallet amount
                }else if(id.equals(transaction.getFrom()))
                {
                    String receiver = endpoints.get(names.indexOf(transaction.getTo()));
                    String json = gsonConverter.toJson(transaction);
                    Payload bytesPayload = Payload.fromBytes(json.getBytes());
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(receiver, bytesPayload);
                    totalBalance-=transaction.getValue();
                    btc.setText(Integer.toString(totalBalance)+" BTC");


                }else
                {
                    //hello
                    Toast.makeText(MainActivity.this, "got a message which is not mine", Toast.LENGTH_SHORT).show();
                    withoutInternet = true;
                    createTransaction(transaction.getAmount(),transaction.getFrom(),transaction.getTo(),transaction.getValue(),transaction.getTime(),endpointId);
                }
            }


            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            }

        };
    }

    private void findViewByIds()
    {
        send = findViewById(R.id.send);
        list = findViewById(R.id.list_view);
        btc = findViewById(R.id.btc);
        enter_amount = findViewById(R.id.enter_amount);
        amountCard = findViewById(R.id.amount_card);
        processing = findViewById(R.id.processing);
        sendtext = findViewById(R.id.sendtext);
        textName = findViewById(R.id.name);

        peopleAdapter = new PeopleAdapter(getApplicationContext(),0,names);
        list.setAdapter(peopleAdapter);

    }

    private void setOnClickListeners()
    {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAdvertising();
                startDiscovery();
                send.setVisibility(View.GONE);
                sendtext.setVisibility(View.GONE);
                amountCard.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                isSender=true;

            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                initiateConnection(endpoints.get(i));
                if(getConnectivityStatus(MainActivity.this)==0)
                {
                    //broadcast to everyone else
                    Log.e("path","no internet");


                    List<String> amount = new ArrayList<>();
                    int value = Integer.parseInt(enter_amount.getText().toString());
                    for(int j=0; j<value/10 ; j++)
                    {
                        amount.add(availableBalance.get(availableBalance.size()-1));
                        availableBalance.remove(availableBalance.size()-1);
                    }
                    //add k1 encryption
                    Date currentTime = Calendar.getInstance().getTime();
                    CreateTransaction transactionResponse = new CreateTransaction(amount,id,names.get(i),value,currentTime.toString());

                    for(int j =0 ; j< endpoints.size(); j++)
                    {
                        if(i!=j) {
                            initiateConnection(endpoints.get(j));
                            resp = transactionResponse;
                            jValue = j;
                            Log.e("endpoint", names.get(j));
                        }
                    }


                }else
                {
                    Log.e("path","with internet");
                    int value = Integer.parseInt(enter_amount.getText().toString());
                    List<String> amount = new ArrayList<>();
                    for(int j=0; j<value/10 ; j++)
                    {
                        amount.add(availableBalance.get(availableBalance.size()-1));
                        availableBalance.remove(availableBalance.size()-1);
                    }
                    //add k1 encryption
                    Date currentTime = Calendar.getInstance().getTime();
                    createTransaction(amount,id,names.get(i),value,currentTime.toString(), endpoints.get(i));

                }
                list.setVisibility(View.GONE);
                processing.setVisibility(View.VISIBLE);


            }
        });

    }

    private void responseCheckForCreateTransaction(CreateTransaction transactionResponse, String endpoint )
    {
        if(transactionResponse.getFrom()!=null) {
            String json = gsonConverter.toJson(transactionResponse);
            Payload bytesPayload = Payload.fromBytes(json.getBytes());
            Toast.makeText(MainActivity.this, "Transaction Successful", Toast.LENGTH_SHORT).show();

            if(withoutInternet)
            {
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(gendpoint, bytesPayload);
            }
            if(connectSuccess)
            {
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(gendpoint, bytesPayload);
            }
            if(isSender)
            {
                transSuccess = true;
                totalBalance-=transactionResponse.getValue();
                btc.setText(Integer.toString(totalBalance)+" BTC");
            }
        }else
        {
            Toast.makeText(MainActivity.this, "Transaction Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void initiateConnection(String endpoint)
    {
        Nearby.getConnectionsClient(getApplicationContext())
                .requestConnection(myName, endpoint, mConnectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "request success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "request failed", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    public static int getConnectivityStatus(Context context) {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return 2;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 1;
            }
        } else {
            return 0;
        }
        return 0;
    }

    //Functions
    public void balance(String id) {
        Call<Balance> req = service.checkBalance(id);
        req.enqueue(new Callback<Balance>() {
            @Override
            public void onResponse(Call<Balance> call, Response<Balance> response) {
                if(response.body()==null)
                {
                    Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }else {
                    Balance res = response.body();
                    Log.e("REQ-SUCCESS", String.valueOf(res.getValue()));
                }
            }

            @Override
            public void onFailure(Call<Balance> call, Throwable t) {
                Log.e("REQ-FAIL", t.getMessage());
            }

        });
    }

    public void checkWallet(String id){
        Call<Wallet> req = service.checkWallet(id);
        req.enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if(response.body() == null){
                    Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
                }
                else{

                    Wallet res = response.body();
                    Log.e("REQ-SUCCESS",res.getTransactions().get(0));

                }
            }

            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {
                Log.e("REQ-FAIL", t.getMessage());
            }
        });
    }

    public CreateTransaction createTransaction(List<String> amount, String from, String to, int value, String time, final String endpoint){
//        List<Integer> amount = new ArrayList<Integer>();
//        amount.add(10);
        Call<CreateTransaction> req = service.createTransaction(new CreateTransaction(amount,from,to,value,time));
        req.enqueue(new Callback<CreateTransaction>() {
            @Override
            public void onResponse(Call<CreateTransaction> call, Response<CreateTransaction> response) {

                if(response.body()==null)
                {
                    Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }else {
                    CreateTransaction  resT = response.body();
                    Log.e("REQ-SUCCESS", resT.getFrom());
                    resp = resT;
                    gendpoint = endpoint;
                    responseCheckForCreateTransaction(resT,endpoint);
                }

            }

            @Override
            public void onFailure(Call<CreateTransaction> call, Throwable t) {

                Log.e("REQ-FAIL", t.getMessage());

            }
        });
        return resp;
    }

    public void checkTransaction(String id){
        Call<CheckTransaction> req = service.checkTransaction(id);
        req.enqueue(new Callback<CheckTransaction>() {
            @Override
            public void onResponse(Call<CheckTransaction> call, Response<CheckTransaction> response) {

                if(response.body()==null)
                {
                    Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }else {
                    CheckTransaction res = response.body();
                    Log.e("REQ-SUCCESS", String.valueOf(res.getAmount().get(0)));
                }

            }

            @Override
            public void onFailure(Call<CheckTransaction> call, Throwable t) {
                Log.e("REQ-FAIL", t.getMessage());
            }
        });
    }

    public void initializeCustomerDetails(String custId){
        Call<Initialization> req = service.initialize(custId);
        req.enqueue(new Callback<Initialization>() {
            @Override
            public void onResponse(Call<Initialization> call, Response<Initialization> response) {

                if(response.body()==null)
                {
                    Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }else {
                    Initialization res = response.body();
                    customerDetails = res;
                    id = res.getAddress();
                    textName.setText(id);
                    availableBalance = res.getAmount();
                    Log.e("listSize",Integer.toString(res.getAmount().size()));
                    send.setVisibility(View.VISIBLE);
                    sendtext.setVisibility(View.VISIBLE);
                    amountCard.setVisibility(View.VISIBLE);
                    totalBalance = res.getEthers();
                    btc.setText(res.getEthers()+" BTC");
                    startAdvertising();
                    Log.e("REQ-SUCCESS", String.valueOf(res.getAddress()));
                }

            }

            @Override
            public void onFailure(Call<Initialization> call, Throwable t) {
                Log.e("REQ-FAIL", t.getMessage());
            }
        });
    }
}
