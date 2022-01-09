import pandas as pd
import os
import csv
import MFCCExtractor
import Server

DATASET = "audio/"


def create_mfcc_dataset():
    # construct the header of the table
    header = ''
    # we have to list the 13 MFCC
    for i in range(1, 14):
        header += f' mfcc{i}'

    for i in range(1, 14):
        header += f' delta{i}'

    for i in range(1, 14):
        header += f' delta_delta{i}'

    header += ' username'
    header = header.split()

    # data.csv will be the new dataset
    file = open('data.csv', 'w', newline='')
    with file:
        writer = csv.writer(file)
        writer.writerow(header)  # Write the header

    for subdir, dirs, files in os.walk(DATASET):
        for filename in files:
            if not filename.startswith('.'):
                to_append = MFCCExtractor.extract_mfcc(os.path.join(subdir, filename))
                to_append += f' {os.path.basename(subdir)}'
                file = open('data.csv', 'a', newline='')
                with file:
                    if not filename.startswith('.'):
                        writer = csv.writer(file)
                        writer.writerow(to_append.split())


def show_dataset():
    data = pd.read_csv('data.csv')
    print(data.head())


if __name__ == '__main__':
    # Server.start_server()
    create_mfcc_dataset()
