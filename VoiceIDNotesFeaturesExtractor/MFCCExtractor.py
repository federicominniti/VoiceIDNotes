import librosa
import numpy as np

SAMPLING_RATE = 16000
NMFCC = 13
HOPLEN = 0.010
FRAME = 0.025


# used to extract MFCCs from a .flac (or .wav) file
def extract_mfcc(path):
    # y = floating point time series of the audio sr = sampling rate (in this case, 16000 Hz)
    y, sr = librosa.load(path, SAMPLING_RATE)
    # for SAMPLING_RATE = 16000, we have to use 13 mfcc.
    # papers show that a hop_length of 10ms and a frame size of 25ms is good for speech recognition
    mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=NMFCC, hop_length=int(HOPLEN * sr), n_fft=int(FRAME * sr))
    delta = librosa.feature.delta(mfcc)
    delta_delta = librosa.feature.delta(mfcc, order=2)
    to_append = ""
    for item in mfcc:  # for each mfcc
        to_append += f' {np.mean(item)}'

    for item in delta:
        to_append += f' {np.mean(delta)}'

    for item in delta_delta:
        to_append += f' {np.mean(delta_delta)}'

    return to_append[1:]
