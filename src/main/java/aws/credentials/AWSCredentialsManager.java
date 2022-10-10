// Author: OrkhanGG
// Created: 10/10/2022
// Purpose: To get the same credentials easily
// See: Create credentials using a provider chain. For more information, see
// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html

package aws.credentials;

import com.amazonaws.auth.AWSCredentials;
import utils.Constants;

public class AWSCredentialsManager implements AWSCredentials {
    private static AWSCredentialsManager awsCredentials = null;


    public static AWSCredentialsManager getInstance(){
        if(awsCredentials == null)
            awsCredentials = new AWSCredentialsManager();

        return awsCredentials;
    }


    @Override
    public String getAWSAccessKeyId() {
        return Constants.AWS_ACCESS_KEY_ID;
    }

    @Override
    public String getAWSSecretKey() {
        return Constants.AWS_SECRET_KEY;
    }

    public String getRegion(){
        return Constants.AWS_REGION;
    }
}
