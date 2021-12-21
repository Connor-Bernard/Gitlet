fileName = input('File Name: ')
fileContents = []
with open(fileName, 'r') as file:
    for line in file:
        fileContents.append(line)
with open(fileName, 'w') as file:
    for line in fileContents:
        if(len(line) <= 5):
            file.write('\n')
        elif(line[1] == '.'):
            file.write(line[3:])
        elif(line[2] == '.'):
            file.write(line[4:])
        else:
            file.write(line[5:])
