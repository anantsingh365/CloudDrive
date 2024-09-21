# CloudDrive

A File Storing application using springboot, the hightlight feature is the ability to have multiple storage Backends (like Aws S3, localFileSystem, Azure Blob etc ) and simultaneous MultiPart file uploads with Pause and Resume functionallity.

![image](https://github.com/user-attachments/assets/12f1b4e5-6e38-4cba-b5e7-4cb327e95f4f)


Pause/Resume looks horrible I know :)  -
![image](https://github.com/user-attachments/assets/03d67388-d77c-4c58-90de-5bcdeeba86c9)


# Installation/Usage  

You need a MySql Database for - storing passwords, user registeration, tiers, upload limits but It's currently kind of all over the place with lots dummy functionality. Application uses hibernate and should be able to auto create the tables in db based on local properties file. Its a mess.

