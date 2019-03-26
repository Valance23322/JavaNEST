import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**This class can be used to interface with a NEST Thermostat via the NEST Cloud API.
 *
 */
public class JavaNEST {

    /**
     *
     */
    private String authToken = "Bearer c.X";

    /**
     *String used to verify the NEST Account associated with the application
     */
    private String clientID = "";

    /**
     *String used to verify the NEST Account associated with the application
     */
    private String clientSecret = "";

    /**
     * URL of NEST Cloud API target endpoint.  All requests other than getAuthToken will be sent to this URL.
     */
    private String nestURL = "https://developer-api.nest.com";

    /**
     * ID that refers to a specific thermostat on the NEST account.
     */
    private String deviceID;



    public static void main(String[] args)
    {
        JavaNEST nest = new JavaNEST();
        System.out.println(nest.getTargetTemp());
    }

    /**
     * Creates an instance of JavaNEST using the hardcoded authToken.  Also sets the first thermostat as the active device.
     */
    public JavaNEST()
    {
        setDeviceID(0);
    }

    /**
     * Fetches a new authToken using the pin code, and hard coded clientSecret + clientID.
     * Also sets the first thermostat as the active device.
     * @param pinCode - Obtained from authorization URL associated with your app (console.developers.nest.com/products)
     */
    public JavaNEST(String pinCode)
    {
        getAuthToken(pinCode);
        setDeviceID(0);
    }

    /**
     * Sets clientID and clientSecret, then fetches a new authToken using pinCode.
     * Also sets the first thermostat as the active device.
     * @param pinCode - Obtained from authorization URL associated with your NEST app (console.developers.nest.com/products)
     * @param clientID - ID associated with your app
     * @param clientSecret - Secret associated with your app
     */
    public JavaNEST(String pinCode, String clientID, String clientSecret)
    {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        getAuthToken(pinCode);
        setDeviceID(0);
    }

    /**Sets the device ID to the nth thermostat on the account (Starting at 0)
     *
     * @param numDevice - This should be 0 to select the first thermostat on the account.
     *                  If an invalid number is entered, will default to 0
     * @return ID of the selected thermostat
     */
    public String setDeviceID(int numDevice)
    {
        Request request = new Request.Builder()
                .url(this.nestURL)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", this.authToken)
                .build();

        JSONObject json = new JSONObject(this.sendRequest(request));

        Iterator<String> itr = json.getJSONObject("devices").getJSONObject("thermostats").keys();
        try
        {
            for(;numDevice > 0; numDevice--)
            {
                itr.next();
            }
            this.deviceID = itr.next();
            return this.deviceID;
        }
        catch(NoSuchElementException ex)
        {
            Iterator<String> itr2 = json.getJSONObject("devices").getJSONObject("thermostats").keys();
            this.deviceID = itr2.next();
            return this.deviceID;
        }

    }

    /**
     *
     * @return
     */
    private OkHttpClient getClient()
    {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if(request.header("Authorization") == null)
                        {
                            request = request.newBuilder()
                                    .addHeader("Authorization", authToken)
                                    .build();
                        }
                        return chain.proceed(request);

                    }
                })
                .build();
    }

    /**
     * Sends the given request using the Client obtained from getClient
     * @param request - request to be sent
     * @return - Response from the given request, as a String
     */
    private String sendRequest(Request request)
    {
        OkHttpClient client = this.getClient();
        try
        {
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
        catch(IOException ex)
        {
            System.out.println("Error sending Request");
            return "Error";
        }
    }

    /**
     *
     * @param pincode
     * @return
     */
    public String getAuthToken(String pincode)
    {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, String.format("client_id=%s&client_secret=%s&grant_type=authorization_code&code=%s",
                clientID, clientSecret, pincode));
        Request request = new Request.Builder()
                .url("https://api.home.nest.com/oauth2/access_token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        String response = sendRequest(request);
        JSONObject json = new JSONObject(response);
        return json.getString("access_token");
    }

    /**
     * Obtains the target temperature currently set on the thermostat.
     * This target is only used if the thermostat is in heat or cool mode.
     * @return - Target temperature (Fahrenheit) or -1 if error occurs
     */
    public int getTargetTemp()
    {

        String url = String.format("%s/devices/thermostats/%s/target_temperature_f", this.nestURL, this.deviceID);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .build();


        String response = sendRequest(request);

        if(response.equals("Error"))
        {
            return -1;
        }
        return Integer.valueOf(response);

    }

    /**
     * Obtains the target High temperature set on the thermostat.
     * This target is only used if the thermostat is in heat-cool mode.
     * @return Target high temperature (Fahrenheit) or -1 if error occurs
     */
    public int getTargetHighTemp()
    {

        return -1;
    }

    /**
     * Obtains the target Low temperature set on the thermostat.
     * This target is only used if the thermostat is in heat-cool mode.
     * @return Target low temperature (Fahrenheit) or -1 if error occurs
     */
    public int getTargetLowTemp()
    {
        return -1;
    }

    /**
     * Sets the target temperature (Fahrenheit) for the thermostat.
     * This target is only used if the thermostat is in heat or cool mode.
     * @param newTarget - Integer value to set the thermostat to
     * @return - The target temperature after attempting to set the target, should be newTarget
     */
    public int setTargetTemp(int newTarget)
    {

        return this.getTargetTemp();
    }
    /**
     * Sets the target high temperature (Fahrenheit) for the thermostat.
     * This target is only used if the thermostat is in heat-cool mode.
     * @param newTarget - Integer value to set the thermostat to
     * @return - The target high temperature after attempting to set the target, should be newTarget
     */
    public int setTargetHighTemp(int newTarget)
    {

        return this.getTargetHighTemp();
    }
    /**
     * Sets the target low temperature (Fahrenheit) for the thermostat.
     * This target is only used if the thermostat is in heat-cool mode.
     * @param newTarget - Integer value to set the thermostat to
     * @return - The target low temperature after attempting to set the target, should be newTarget
     */
    public int setTargetLowTemp(int newTarget)
    {

        return this.getTargetLowTemp();
    }



}
