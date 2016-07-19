#!/usr/bin/ruby
=begin
  Copyright (c) 2016-present, Facebook, Inc.
  All rights reserved.
  
  This source code is licensed under the BSD-style license found in the
  LICENSE file in the root directory of this source tree.
=end

require 'rubygems'
require 'json'
require_relative 'ae_comp_conversion'
require 'zlib'

valuesJson = JSON.parse(File.open(ARGV[0], "r").read)

def gzipText(filename, text)
  puts "generating: " + filename
  Zlib::GzipWriter.open(filename) do |gz|
    gz.write text
  end
end

facesArray = Array.new
test = false;
for item in valuesJson["items"]
  # puts "marp " + item.to_s
  if (!item["name"].start_with?("Ray - Facebook Reactions"))
    faceFileName = item["name"].tr('^A-Za-z', '').downcase + ".3f_uncompressed"
    puts faceFileName
    faceHash = convertFunction(item)
    faceHash["canvas_size"] = [177, 177]
    file = File.new(faceFileName, "w+")
    file.puts(JSON.generate(faceHash))
    facesArray.push(faceHash)

    gzipFaceFileName = item["name"].tr('^A-Za-z', '').downcase + ".3f"
    gzipText(gzipFaceFileName, JSON.generate(faceHash))
  end
end

file = File.new("reactions_faces.3f_uncompressed", "w+")
file.puts(JSON.generate(facesArray))
file.close

gzipText("reactions_faces.3f", JSON.generate(facesArray))
