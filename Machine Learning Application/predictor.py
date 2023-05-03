import numpy as np
from os import path
import io
import librosa
import tflite_runtime.interpreter as tflite
import sys

class QuadPredictor():
    def __init__(self, input_file):
        """Constructor method
        """
        print("constructor hit")
        self.output_text = ""
        
        # extract spectrogram
        spec_array = self.create_spectrogram(input_file)

        # predict!
        self.format_input = input_file.split('.')[-1]
        self.predict_and_save(spec_array)


    def create_spectrogram(self, in_f, sr=16000, win_length=1024, hop_length=512, num_mel=128):
        """This method creates a melspectrogram from an audio file using librosa
        audio processing library. Parameters are default from Han et al.
        
        :param filename: wav filename to process.
        :param sr: sampling rate in Hz (default: 16000).
        :param win_length: window length for STFT (default: 1024).
        :param hop_length: hop length for STFT (default: 512).
        :param num_mel: number of mel bands (default:128).
        :type filename: str
        :type sr: int
        :type win_length: int
        :type hop_length: int
        :type num_mel: int
        
        :returns: **ln_S** *(np.array)* - melspectrogram of the complete audio file with logarithmic compression with dimensionality [mel bands x time frames].
        """
        # minimum float32 representation epsilon in python
        eps = np.finfo(np.float32).eps
        assert path.exists(in_f), "filename %r does not exist" % in_f

        data, sr = librosa.load(in_f, sr=sr, mono=True)
        duration = int(np.floor(sr / hop_length))
        try:
            # normalize data
            data /= np.max(np.abs(data))
        except Warning:
            print(in_f, ' is empty')

        # time-frequency representation Short Time Fourier Transform
        D = np.abs(librosa.stft(data, win_length=win_length, hop_length=hop_length, center=True))
        # mel frequency representation
        S = librosa.feature.melspectrogram(S=D, sr=sr, n_mels=num_mel)
        # apply natural logarithm
        ln_S = np.log(S + eps)
        spec_list = []
        for idx in range(0, ln_S.shape[1] - duration + 1, duration):
            # append chunk of spectrogram to dataset
            spec_list.append(ln_S[:, idx:(idx + duration)])
        spec_array = np.expand_dims(spec_list, axis=1)
        return spec_array


    def predict_and_save(self, spec_array):
        """ This method makes predictions and saves the output in several forms 
        depending on the initial config. 
        """

        #Initialises the Tensorflow Lite interpreter
        interpreter = tflite.Interpreter(model_path="newModel.tflite")
        interpreter.allocate_tensors()

        #resize tensor input to match input data
        interpreter.resize_tensor_input(0, list(spec_array.shape))
        interpreter.allocate_tensors()
        new_input_details = interpreter.get_input_details()
        interpreter.set_tensor(new_input_details[0]["index"], spec_array)
        

        # Run inference
        interpreter.invoke()

        # Get output data
        output_details = interpreter.get_output_details()
        mean_predictions = interpreter.get_tensor(output_details[0]['index'])
        arousal = interpreter.get_tensor(output_details[1]['index'])
        valence = interpreter.get_tensor(output_details[2]['index'])

        y_pred_quad = arousal.tolist()
        y_pred_arou = mean_predictions.tolist()
        y_pred_vale = valence.tolist()         
   
        mean_pred_quad = np.mean(y_pred_quad, axis=0)

        mean_pred_arou = np.mean(y_pred_arou, axis=0)
        mean_pred_vale = np.mean(y_pred_vale, axis=0)

        #Store output and return it to Kivy to display to user
        output = io.StringIO()
        sys.stdout = output
        
        print('*************\nMean predictions for ', self.format_input)
        print('Quadrant 1 (positive arousal, positive valence):', mean_pred_quad[0])
        print('Quadrant 2 (positive arousal, negative valence):', mean_pred_quad[1])
        print('Quadrant 3 (negative arousal, negative valence):', mean_pred_quad[2])
        print('Quadrant 4 (negative arousal, positive valence):', mean_pred_quad[3])
        print('*************')

        print('Negative arousal:', mean_pred_arou[0])
        print('Positive arousal:', mean_pred_arou[1])
        print('*************')
        print('Negative valence:', mean_pred_vale[0])
        print('Positive valence:', mean_pred_vale[1])
        print('*************')
        sys.stdout = sys.__stdout__
        output_str = output.getvalue()
        self.output_text = output_str




if __name__ == "__main__":
    q_pred = QuadPredictor('test.wav')
