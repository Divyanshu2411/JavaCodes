/**
Requirements:
- ls (path)
-> if path ==file, return file
-> if path == directory return directory files in lexicorgaphical order
- mkdir(path)
-> create new directory, if any of the direcctoy in the path doesn't exist, create them too
-> if directory already exists, then throw an error

    - addContentToFile(filePath,content)
        -> if file doesn't exist, create the file and add content
        -> it it exists, append to original contentn


// essentially it's like a tree, with intermediate nodes being directories which can have directories as children or directories themselves.

    ### Entities

    #CompositeFile
    - isFile -> boolean
    - List<CompositeFile> -> list of children composite file
    - Parent - Composite file -> to support ..
    - content -> string

    
    + addFile(path,content)
    + move(path)
    + ls(path, content)
    + readContent(path,content)
    
     */