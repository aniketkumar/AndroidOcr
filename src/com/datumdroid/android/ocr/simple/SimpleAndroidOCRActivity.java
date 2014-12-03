package com.datumdroid.android.ocr.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

public class SimpleAndroidOCRActivity extends Activity {
	public static final String PACKAGE_NAME = "com.datumdroid.android.ocr.simple";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
	
	public String repeat = "";
	
	public static final String lang = "eng";

	private static final String TAG = "SimpleAndroidOCR.java";

	protected Button _button;
	// protected ImageView _image;
	protected EditText _field;
	protected String _path;
	protected boolean _taken;

	protected static final String PHOTO_TAKEN = "photo_taken";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		startService(new Intent(this, launcher.class));
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}

		}

		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
				
				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// _image = (ImageView) findViewById(R.id.image);
		_field = (EditText) findViewById(R.id.field);
		_button = (Button) findViewById(R.id.button);
		_button.setOnClickListener(new ButtonClickHandler());
		
		
		/* code for repeat goes here */
		Button b = (Button)findViewById(R.id.button1);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inte = new Intent(SimpleAndroidOCRActivity.this, TTS.class);
				inte.putExtra("text", repeat);
				startActivity(inte);
			}
		});
		
		

		_path = DATA_PATH + "/ocr.jpg";
	}

	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			Log.v(TAG, "Starting Camera app");
			startCameraActivity();
		}
	}
	

	protected void startCameraActivity() {
		File file = new File(_path);
		Uri outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "resultCode: " + resultCode);

		if (resultCode == -1) {
			onPhotoTaken();
		} else {
			Log.v(TAG, "User cancelled");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(SimpleAndroidOCRActivity.PHOTO_TAKEN, _taken);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onRestoreInstanceState()");
		if (savedInstanceState.getBoolean(SimpleAndroidOCRActivity.PHOTO_TAKEN)) {
			onPhotoTaken();
		}
	}

	public void onPhotoTaken() {
		_taken = true;
		
		_field.setText("");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// _image.setImageBitmap( bitmap );
		
		Log.v(TAG, "Before baseApi");
		
		/********using preprocessing ********/
		
		final Bitmap bm = Bitmap.createBitmap(bitmap);
		//final Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.razzz);
		
        
        BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    	    @Override
    	    public void onManagerConnected(int status) {
    	        if (status == LoaderCallbackInterface.SUCCESS ) {
    	            // now we can call opencv code !
    	        	//setContentView(R.layout.activity_preprocessing);
    	            //String path = Environment.getExternalStorageDirectory().toString();
    	            
    	            //TextView t = (TextView) findViewById(R.id.textView1);
    	            ImageView image = (ImageView) findViewById(R.id.imageView1);
    	            //getting bitmap here
    	            
    	            //bm = binarize(bm);
    	            
    	            
    	            /** initial setting **/
    	            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
    	            bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
    	            
    	            //Mat ImageMat = new Mat();
    	            //Utils.bitmapToMat(bm, ImageMat); 
    	            
    	            //converting bitmap to mat
    	            Mat srcMat = new Mat ( bm.getHeight(), bm.getWidth(), CvType.CV_8UC3);
    	            Bitmap myBitmap32 = bm.copy(Bitmap.Config.ARGB_8888, true);
    	            Utils.bitmapToMat(myBitmap32, srcMat);
    	            

    	            //Imgproc.GaussianBlur(srcMat, srcMat, new Size(7,7), 0);
    	            
    	            //binarization with greyscale
    	            
    	            Mat destination = new Mat(srcMat.rows(),srcMat.cols(),srcMat.type());

            		Imgproc.cvtColor(srcMat, destination, Imgproc.COLOR_RGB2GRAY);

            		Mat destination2 = new Mat(srcMat.rows(),srcMat.cols(),srcMat.type());

            		Imgproc.threshold(destination, destination2, 0, 255,
        	                Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
            		//Imgproc.adaptiveThreshold(destination, destination2, 255,
            		//Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
            		
            		//binarization ends


    	           
    	            
//    	            //applying canny 
//    	            Mat edge = new Mat();
//    	            Mat dst = new Mat();
//    	            Mat dst1 = new Mat();
//    	            Imgproc.Canny(destination2, edge, 80, 90);
//    	            Imgproc.cvtColor(edge, dst, Imgproc.COLOR_GRAY2RGBA,4);
    	            
    	            Mat dst1 = new Mat();
    	            

            		/************deskewing***************/
    	            Mat lines = new Mat();
    	            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(5, 3));
    	            Imgproc.erode(destination2, dst1, element);
    	            
    	            
    	            List<org.opencv.core.Point> pList = new ArrayList<org.opencv.core.Point>();
    	            for (int k = 0; k < dst1.size().height; k++) {
    	                for (int k2 = 0; k2 < dst1.size().width; k2++) {
    	                    Scalar color = new Scalar(dst1.get(k2, k));
    	                    if (color.val[0] == 255) {
    	                        
    	                    	
    	                    	pList.add(new org.opencv.core.Point(k2, k));
    	                    } 
    	                }
    	            }
    	            org.opencv.core.Point[] pArray = new org.opencv.core.Point[pList.size()];
    	            for (int k = 0; k < pArray.length; k++) {
    	                pArray[k] = pList.get(k);
    	                
    	            }
    	            MatOfPoint2f pointsInterest = new MatOfPoint2f(pArray);
    	            
    	            org.opencv.core.RotatedRect box = Imgproc.minAreaRect(pointsInterest);
    	            

    	            double angle = box.angle;
    	            
    	            String s = "" + angle;
    	            if (angle <= -45.){
    	            	angle += 90.;
    	            	angle = -1 * angle;
    	            	s = s + " inside";
    	            } else {
    	            	angle = Math.abs(angle);
    	            }
    	            s = s + " " + angle;
    	            //t.setText(s);
//    	            angle = Math.abs(angle);
//    	            angle = -1 * angle;
    	            org.opencv.core.Point center = new org.opencv.core.Point(destination2.cols()/2, destination2.rows()/2);
    	            Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1);
    	            Mat warp_dst = new Mat();
    	            Imgproc.warpAffine(destination2, warp_dst, rotImage, destination2.size(), Imgproc.INTER_CUBIC);
                
    	            
    	            
    	            
    	          //convering back to bitmap
    	            Bitmap resultBitmap = Bitmap.createBitmap(warp_dst.cols(), warp_dst.rows(),Bitmap.Config.ARGB_8888);           
    	            Utils.matToBitmap(warp_dst, resultBitmap);
    	            image.setImageBitmap(resultBitmap);
    	            
    	            Log.v("abcd", "xyz");
    	            
    	            TessBaseAPI baseApi = new TessBaseAPI();
    	    		baseApi.setDebug(true);
    	    		baseApi.init(DATA_PATH, lang);
    	    		Log.v("abcd1", "xyz1");
    	    		baseApi.setImage(resultBitmap);
    	    		Log.v("abcd2", "xyz2");
    	    		String recognizedText = baseApi.getUTF8Text();
    	    		Log.v("abcd3", "xyz3");
    	    		
    	    		baseApi.end();
    	    		Log.v(TAG, "OCRED TEXT: " + recognizedText);
    	    		Log.v("abcd4", "xyz4");
    	    		if ( lang.equalsIgnoreCase("eng") ) {
    	    			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
    	    		}
    	    		
    	    		recognizedText = recognizedText.trim();
    	    		
    	    		repeat = recognizedText;

    	    		if ( recognizedText.length() != 0 ) {
    	    			_field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
    	    			_field.setSelection(_field.getText().toString().length());
    	    		}
    	    		
    	    		
    	    		//tts code goes here :P
    	    		Intent inte = new Intent(SimpleAndroidOCRActivity.this, TTS.class);
    	    		inte.putExtra("text", recognizedText);
    	    		startActivity(inte);
    	        } else {
    	            super.onManagerConnected(status);
    	        }
    	    }
    	};
        
    	
    	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,this, mLoaderCallback);
		
		/*********preprocessing **********/
		
		// Cycle done.
	}
	
}
