#! /usr/bin/env bash
set -eu

# Locate the script file.  Cross symlinks if necessary.
loc="$0"
while [ -h "$loc" ]; do
    ls=`ls -ld "$loc"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        loc="$link"  # Absolute link
    else
        loc="`dirname "$loc"`/$link"  # Relative link
    fi
done
base_dir=$(cd "`dirname "$loc"`" && pwd)

USER_TEMPLATES_PATH="Library/Developer/Xcode/Templates/File Templates/User Templates"

function copy_template {
    need_to_copy=0
    name=$1
    source_dir="$base_dir/$name.xctemplate"
    user_template_dir="$USER_TEMPLATES_PATH/$name.xctemplate"
    if [ ! -d ~/"$user_template_dir" ]; then
        echo "Template not exist ~/$user_template_dir"
        need_to_copy=1
    else
        newer=$(find "$source_dir" -newer ~/"$user_template_dir" | head | wc -l)
        if [ "$newer" -gt 0 ]; then
            echo "Found newer"
            need_to_copy=1
            rm -rf ~/"$user_template_dir"
        fi
    fi

    if [ "$need_to_copy" -eq 1 ]; then
        echo "Copying template - $name"
        cp -r "$source_dir" ~/"$user_template_dir"
    fi
}

if [ ! -d ~/"$USER_TEMPLATES_PATH" ]; then
    echo "copying Templates"
    mkdir -p ~/"$USER_TEMPLATES_PATH"
fi

copy_template Bicycle-Cpp-File
copy_template Bicycle-Cpp-Header