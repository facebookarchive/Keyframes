#!/usr/bin/ruby

require 'rubygems'
require 'json'

class MoveCommand
  def initialize(ex, ey)
    @ex = ex
    @ey = ey
  end

  def toNiceString
    return "M"+toString(@ex)+","+toString(@ey)
  end

  def offset(offX, offY)
    @ex += offX
    @ey += offY
  end
end

class QuadraticCommand
  def initialize(c1x, c1y, ex, ey)
    @c1x = c1x
    @c1y = c1y
    @ex = ex
    @ey = ey
  end

  def toNiceString
    return "Q"+toString(@c1x)+","+toString(@c1y)+","+toString(@ex)+","+toString(@ey)
  end

  def offset(offX, offY)
    @c1x += offX
    @c1y += offY
    @ex += offX
    @ey += offY
  end
end

class CubicCommand
  def initialize(c1x, c1y, c2x, c2y, ex, ey)
    @c1x = c1x
    @c1y = c1y
    @c2x = c2x
    @c2y = c2y
    @ex = ex
    @ey = ey
  end

  def toNiceString
    return "C"+toString(@c1x)+","+toString(@c1y)+","+toString(@c2x)+","+toString(@c2y)+","+toString(@ex)+","+toString(@ey)
  end

  def offset(offX, offY)
    @c1x += offX
    @c1y += offY
    @c2x += offX
    @c2y += offY
    @ex += offX
    @ey += offY
  end
end

class LineCommand
  def initialize(ex, ey)
    @ex = ex
    @ey = ey
  end

  def toNiceString
    return "L"+toString(@ex)+","+toString(@ey)
  end

  def offset(offX, offY)
    @ex += offX
    @ey += offY
  end
end

def toString(numberF)
  return numberF.round(2).to_s
end

def hasTangent(pair)
  return pair[0] != 0 || pair[1] != 0
end

def getTangentCount(pair1, pair2)
  count = 0
  if (hasTangent(pair1))
    count+=1
  end
  if (hasTangent(pair2))
    count+=1
  end
  return count
end

def quad_to_cubic(commandList)
  start = commandList[0]
  command = commandList[1]

  splitStart = start[1..start.length].split(",")
  splitCommand = command[1..command.length].split(",")

  sx = splitStart[0].to_f
  sy = splitStart[1].to_f
  c1x = splitCommand[0].to_f
  c1y = splitCommand[1].to_f
  ex = splitCommand[2].to_f
  ey = splitCommand[3].to_f

  cc1x = (sx + 2.0/3.0 * (c1x - sx)).round(2)
  cc1y = (sy + 2.0/3.0 * (c1y - sy)).round(2)
  cc2x = (ex + 2.0/3.0 * (c1x - ex)).round(2)
  cc2y = (ey + 2.0/3.0 * (c1y - ey)).round(2)

  commandList[1] = "C"+cc1x.to_s+","+cc1y.to_s+","+cc2x.to_s+","+cc2y.to_s+","+ex.to_s+","+ey.to_s
  return commandList
end

def sanitizeKeyFrameMoveLists(keyFrameArray)
  if (keyFrameArray.length <= 1)
    return keyFrameArray
  end
  quadFound = false
  cubicFound = false
  for keyFrame in keyFrameArray
    if keyFrame["data"][1].start_with?("Q")
      quadFound = true
    elsif keyFrame["data"][1].start_with?("C")
      cubicFound = true
    end
  end
  if quadFound && cubicFound
    for keyFrame in keyFrameArray
      if (keyFrame["data"][1].start_with?("Q"))
        keyFrame["data"] = quad_to_cubic(keyFrame["data"])
      end
    end
  end
  return keyFrameArray
end

def colorFromRGB(colors, layerOpacity)
  r = colors[0] * 255
  g = colors[1] * 255
  b = colors[2] * 255
  a = colors[3] * layerOpacity * 255
  return "#" + (a.round.to_s(16).rjust(2, '0') + r.round.to_s(16).rjust(2, '0') + g.round.to_s(16).rjust(2, '0') + b.round.to_s(16).rjust(2, '0')).downcase
end

def parseAnimationCurves(animationKeyFramesJson, prevIndex, currIndex)
  controlPoint1 = [animationKeyFramesJson[prevIndex]["outTemporalEase"][0]["influence"].to_f/100, 0]
  controlPoint2 = [1 - animationKeyFramesJson[currIndex]["inTemporalEase"][0]["influence"].to_f/100, 1]
  return [controlPoint1, controlPoint2]
end

# Calculates key frame interpolation curve between first and second key frame.
# progress is from 0 to 1.
def parseTimingFunctions(keyFramesJson, prevIndex, currIndex)
  # prev out & curr in
  controlPoint1 = [keyFramesJson[prevIndex]["outTemporalEase"][0]["influence"].to_f/200, 0]
  controlPoint2 = [1 - keyFramesJson[currIndex]["inTemporalEase"][0]["influence"].to_f/200, 1]
  return [controlPoint1, controlPoint2]
end

def parseKeyframe(json, offset)
  inTangents = json["inTangents"]
  outTangents = json["outTangents"]
  vertices = json["vertices"]

  result = Array.new()
  vertex = vertices[0]
  result.push(MoveCommand.new(vertex[0], vertex[1]))

  for i in 1..vertices.length
    if (i == vertices.length)
      prevIndex = vertices.length-1
      currIndex=0
    else
      prevIndex = i - 1
      currIndex = i
    end
    prevVertex = vertices[prevIndex]
    currVertex = vertices[currIndex]
    tangentCount = getTangentCount(outTangents[prevIndex], inTangents[currIndex])

    if (tangentCount == 2)
      result.push(CubicCommand.new(
        prevVertex[0] + outTangents[prevIndex][0],
        prevVertex[1] + outTangents[prevIndex][1],
        currVertex[0] + inTangents[currIndex][0],
        currVertex[1] + inTangents[currIndex][1],
        currVertex[0],
        currVertex[1]))
      elsif (tangentCount == 1)
        if (hasTangent(outTangents[prevIndex]))
          result.push(QuadraticCommand.new(
          prevVertex[0] + outTangents[prevIndex][0],
          prevVertex[1] + outTangents[prevIndex][1],
          currVertex[0],
          currVertex[1]))
        elsif (hasTangent(inTangents[currIndex]))
          result.push(QuadraticCommand.new(
          currVertex[0] + inTangents[currIndex][0],
          currVertex[1] + inTangents[currIndex][1],
          currVertex[0],
          currVertex[1]))
        end
      elsif (tangentCount == 0)
        result.push(LineCommand.new(
        currVertex[0],
        currVertex[1]))
      end
    end

    niceStringResult = Array.new()
    for i in 0...result.length
      result[i].offset(offset[0], offset[1]) if not offset.nil?
      niceStringResult.push(result[i].toNiceString)
  end
  return niceStringResult
end

def extractStrokeWidthKeyframes(strokeKeyframesHash, frame_rate)
  # puts "found key frame for stroke width"
  keyValues = strokeKeyframesHash.map { |keyframe|
    {
      "start_frame" => (keyframe["time"] * frame_rate).round,
      "data" => keyframe["value"].class == Array && keyframe["value"].length > 2 ? keyframe["value"][0, 2] : [keyframe["value"]]
    }
  }
  timingFunctions = Array.new()
  strokeKeyframesHash.each_with_index { |keyframe, index|
    if (index > 0)
      timingFunctions.push(parseTimingFunctions(strokeKeyframesHash, index-1, index))
    end
  }
  return {
    "property" => "STROKE_WIDTH",
    "key_values" => keyValues,
    "timing_curves" => timingFunctions
  }
end

# Has a side effect of trimming keyFrameHash's last data object if we are dealing with stroke instead of fill.
# returns array of [ColorHash, animation(if found)]
def extractColors(keyFrameHash, propertyGroup, frame_rate, layerOpacity)
  colorHash = Hash.new()
  animation = nil
  foundStroke = false
  foundFill = false
  for property in propertyGroup
    if property["name"].eql?("Stroke 1")
      foundStroke = true
      for strokeProperty in property["properties"]
        if strokeProperty["name"].eql?("Color")
          colorHash["stroke_color"] = colorFromRGB(strokeProperty["value"], layerOpacity)
        elsif strokeProperty["name"].eql?("Stroke Width")
          colorHash["stroke_width"] = strokeProperty["value"]
          if (strokeProperty["keyframes"])
            animation = extractStrokeWidthKeyframes(strokeProperty["keyframes"], frame_rate)
          end
        end
      end
      # puts "origData" + keyFrameHash["data"].to_s
      # puts "newData" + keyFrameHash["data"].to_s
      # puts "marp stroke " + keyFrameHash["stroke_color"].to_s + " " + keyFrameHash["stroke_width"].to_s
    elsif property["name"].eql?("Fill 1")
      foundFill = true
      for fillProperty in property["properties"]
        if fillProperty["name"].eql?("Color")
          colorHash["fill_color"] = colorFromRGB(fillProperty["value"], layerOpacity)
        end
      end
      # puts "marp fill " + keyFrameHash["fill_color"].to_s
    end
  end

  if (foundStroke == true && foundFill == false)
    keyFrameHash["data"].slice!(-1)
  end
  return [colorHash, animation]
end

def extractLayerAnimationInfo(layerProperties, frame_rate, canvas_size)
  # animation data is always in the following order:
  # anchor -> position -> scale -> rotation -> opacity
  animationsArray = Array.new()
  anchorPoint = [canvas_size[0]/2, canvas_size[1]/2]

  if (layerProperties[0]["propertyValueType"].eql?("MARKER"))
    layerPropertyToLoopOver = layerProperties[1]["properties"]
  else
    layerPropertyToLoopOver = layerProperties[0]["properties"]
  end

  for layerProperty in layerPropertyToLoopOver
    # set default anchor to middle
    if (layerProperty["name"].eql?("Anchor Point"))
      anchorPoint = layerProperty["value"][0..1]
    elsif (layerProperty["name"].eql?("Position") ||
      layerProperty["name"].eql?("Rotation") ||
      layerProperty["name"].eql?("Scale"))
      if (layerProperty["name"].eql?("Position"))
        anchorPoint = layerProperty["value"][0..1]
      end
      if (layerProperty["keyframes"])
        keyValues = layerProperty["keyframes"].map { |keyframe|
          {
            "start_frame" => (keyframe["time"] * frame_rate).round,
            "data" => keyframe["value"].class == Array && keyframe["value"].length > 2 ? keyframe["value"][0, 2] : [keyframe["value"]]
          }
        }
        timingFunctions = Array.new()
        layerProperty["keyframes"].each_with_index { |keyframe, index|
          if (index > 0)
            timingFunctions.push(parseTimingFunctions(layerProperty["keyframes"], index-1, index))
          end
        }
        animationsArray.push({
          "property" => layerProperty["name"].upcase,
          "key_values" => keyValues,
          "timing_curves" => timingFunctions,
          "anchor" => anchorPoint
        })
      end
    end
  end
  return animationsArray
end

@animationGroupIdHash = {}
@animationGroupIdCounter = 1

def extractShapeLayerInfo(layersArray, layerProperties, layerJson, fileHash)
  layerHash = Hash.new()
  colorAnimation = nil
  layerOpacity = 1.0

  for layerProperty in layerProperties
    if (layerProperty["name"].eql?("Contents"))
      for contentsProperty in layerProperty["properties"]
        shapeProperty = contentsProperty
        until shapeProperty.nil? || shapeProperty["propertyValueType"].eql?("SHAPE") do
          if (!shapeProperty["properties"].nil?)
            if (shapeProperty["properties"][0]["name"].eql?("Shape Direction"))
              shapeProperty = shapeProperty["properties"][1]
            else
              shapeProperty = shapeProperty["properties"][0]
            end
          else
            shapeProperty = nil
          end
        end
        unless shapeProperty.nil?
          # we found our shape layer
          layerHash["name"] = layerJson["name"]
          # puts layerJson["name"]
          layerHash["animation_group"] = @animationGroupIdHash[layerJson["parent$name"]] if !layerJson["parent$name"].nil?

          keyFramesArray = Array.new()
          timingFunctionsArray = Array.new()
          opacity = 1.0

          transformProperties = layerProperty["properties"][0]["properties"][1]
          if (!transformProperties.nil? && transformProperties["name"].eql?("Transform"))
            for transformProperty in transformProperties["properties"]
              if (transformProperty["name"].eql?("Position"))
                shapeOffsetPosition = transformProperty["value"]
              elsif (transformProperty["name"].eql?("Opacity"))
                opacity = transformProperty["value"] * 1.0 / 100
              end
            end
          end

          colorHash = nil
          if (shapeProperty["keyframes"])
            shapeProperty["keyframes"].each_with_index { |keyFrame, index|
              keyFrameHash = Hash.new()
              keyFrameHash["start_frame"] = (keyFrame["time"] * fileHash["frame_rate"]).round
              keyFrameHash["data"] = parseKeyframe(keyFrame["value"], shapeOffsetPosition)
              if (index > 0)
                timingFunctionsArray.push(parseTimingFunctions(shapeProperty["keyframes"], index-1, index))
              end
              colorArray = extractColors(keyFrameHash, contentsProperty["properties"][0]["properties"], fileHash["frame_rate"], opacity)
              colorHash = colorArray[0]
              colorAnimation = colorArray[1]
              keyFramesArray.push(keyFrameHash)
            }
          else
            keyFrameHash = Hash.new()
            keyFrameHash["start_frame"] = 0
            keyFrameHash["data"] = parseKeyframe(shapeProperty["value"], shapeOffsetPosition)
            colorArray = extractColors(keyFrameHash, contentsProperty["properties"][0]["properties"], fileHash["frame_rate"], opacity)
            colorHash = colorArray[0]
            colorAnimation = colorArray[1]
            keyFramesArray.push(keyFrameHash)
          end

          if !colorHash.nil?
            for item in colorHash.keys
              layerHash[item] = colorHash[item]
            end
          end
          layerHash["key_frames"] = sanitizeKeyFrameMoveLists(keyFramesArray)
          layerHash["timing_curves"] = timingFunctionsArray if timingFunctionsArray.length > 0
          layerOpacity = opacity if !opacity.nil?
        end
      end
    elsif (layerProperty["name"].eql?("Transform"))
      featureAnimationsArray = Array.new
      for transformProperty in layerProperty["properties"]
        if (transformProperty["name"].eql?("Position"))
          shapeOffsetPosition = transformProperty["value"][0, 2]
        end
        if (!transformProperty["keyframes"].nil?)
          # Extract feature shape property animation (only happens in LIKE for now)
          shapeLayerAnimationKeyFramesArray = Array.new()
          shapeLayerAnimationTimingFunctionsAray = Array.new()

          transformProperty["keyframes"].each_with_index { |keyFrame, index|
            keyFrameHash = Hash.new
            keyFrameHash["start_frame"] = (keyFrame["time"] * fileHash["frame_rate"]).round
            keyFrameHash["data"] = keyFrame["value"].class == Array && keyFrame["value"].length > 2 ? keyFrame["value"][0, 2] : [keyFrame["value"]]
            shapeLayerAnimationKeyFramesArray.push(keyFrameHash)
            if (index > 0)
              shapeLayerAnimationTimingFunctionsAray.push(parseAnimationCurves(transformProperty["keyframes"], index-1, index))
            end
          }
          featureAnimation = Hash.new
          featureAnimation["property"] = transformProperty["name"].upcase
          featureAnimation["key_values"] = shapeLayerAnimationKeyFramesArray
          featureAnimation["timing_curves"] = shapeLayerAnimationTimingFunctionsAray
          if (featureAnimation["property"].eql?("ROTATION") ||
            featureAnimation["property"].eql?("SCALE"))
            featureAnimation["anchor"] = shapeOffsetPosition
          end
          featureAnimationsArray.push(featureAnimation)
        end
      end
      layerHash["feature_animations"] = featureAnimationsArray if featureAnimationsArray.length > 0
    elsif (layerProperty["name"].eql?("Effects"))
      effectsHash = Hash.new
      for effectsProperty in layerProperty["properties"]
        if (effectsProperty["name"].eql?("Gradient Ramp"))
          gradientHash = Hash.new
          for gradientsProperty in effectsProperty["properties"]
            if (gradientsProperty["name"].nil? or gradientsProperty["name"].length == 0)
              next
            end

            if (gradientsProperty["name"].eql?("Ramp Shape"))
              if (gradientsProperty["value"] == 1)
                gradientHash["gradient_type"] = "linear"
              elsif (gradientsProperty["value"] == 2)
                gradientHash["gradient_type"] = "radial"
              end
              next
            end

            keyName = nil;
            if (gradientsProperty["name"].eql?("Start of Ramp"))
              keyName = "ramp_start"
            elsif (gradientsProperty["name"].eql?("End of Ramp"))
              keyName = "ramp_end"
            elsif (gradientsProperty["name"].eql?("Start Color"))
              keyName = "color_start"
            elsif (gradientsProperty["name"].eql?("End Color"))
              keyName = "color_end"
            end

            gradientKeyFramesArray = Array.new
            gradientTimingFunctionsArray = Array.new

            if (!gradientsProperty["keyframes"].nil?)
              gradientsProperty["keyframes"].each_with_index { |keyFrame, index|
                keyFrameHash = Hash.new
                keyFrameHash["start_frame"] = (keyFrame["time"] * fileHash["frame_rate"]).round

                if (gradientsProperty["name"].eql?("Start Color") ||
                  gradientsProperty["name"].eql?("End Color"))
                  keyFrameHash["data"] = colorFromRGB(keyFrame["value"], layerOpacity)
                else
                  keyFrameHash["data"] = keyFrame["value"].class == Array && keyFrame["value"].length > 2 ? keyFrame["value"][0, 2] : [keyFrame["value"]]
                end
                gradientKeyFramesArray.push(keyFrameHash)
                if (index > 0)
                  gradientTimingFunctionsArray.push(parseAnimationCurves(gradientsProperty["keyframes"], index-1, index))
                end
              }
            else
              keyFrameHash = Hash.new
              if (gradientsProperty["name"].eql?("Start Color") ||
                gradientsProperty["name"].eql?("End Color"))
                keyFrameHash["data"] = colorFromRGB(gradientsProperty["value"], layerOpacity)
              else
                keyFrameHash["data"] = gradientsProperty["value"]
              end
              keyFrameHash["start_frame"] = 0
              gradientKeyFramesArray.push(keyFrameHash)
            end

            hashValue = Hash.new
            hashValue["key_values"] = gradientKeyFramesArray
            hashValue["timing_curves"] = gradientTimingFunctionsArray if gradientTimingFunctionsArray.length > 0
            gradientHash[keyName] = hashValue if !keyName.nil?
          end
          if (gradientHash.length > 0)
            if (gradientHash["gradient_type"].nil?)
              gradientHash["gradient_type"] = "linear"
            end
            effectsHash["gradient"] = gradientHash
          end
        end
      end
      layerHash["effects"] = effectsHash if effectsHash.keys.length > 0
    end
  end

  if (!colorAnimation.nil?)
    if (!layerHash["feature_animations"].nil? && layerHash["feature_animations"].length)
      layerHash["feature_animations"].push(colorAnimation)
    else
      layerHash["feature_animations"] = [colorAnimation]
    end
  end
  layersArray.unshift(layerHash) if layerHash.length > 0
end

def convertFunction(faceJson)

  fileHash = Hash.new()
  fileHash["name"] = faceJson["name"].tr('^A-Za-z', '').upcase
  fileHash["key"] = faceJson["name"].tr('^0-9', '').to_i
  fileHash["frame_rate"] = faceJson["frameRate"]
  fileHash["animation_frame_count"] = (faceJson["frameRate"] * faceJson["workAreaDuration"]).round

  layersJson = faceJson["layers"]
  layersArray = Array.new()
  canvas_size = [faceJson["width"], faceJson["height"]]
  animationGroups = Array.new

  for layerJson in layersJson
    # loop through all properties

    layerProperties = layerJson["properties"]

    # There could be multiple AVLayer. For now, just use the first one.
    if (layerJson["__type__"].eql?("AVLayer") && fileHash["animations"].nil?)
      animationGroupHash = Hash.new
      if (!@animationGroupIdHash.include? layerJson["name"])
        @animationGroupIdHash[layerJson["name"]] = @animationGroupIdCounter
        @animationGroupIdCounter += 1
      end
      animationGroupHash["group_id"] = @animationGroupIdHash[layerJson["name"]]
      animationGroupHash["group_name"] = layerJson["name"]
      animationGroupHash["parent_group"] = @animationGroupIdHash[layerJson["parent$name"]] if !layerJson["parent$name"].nil?
      animationGroupHash["animations"] = extractLayerAnimationInfo(
        layerProperties,
        fileHash["frame_rate"],
        canvas_size
        )
      animationGroups.push(animationGroupHash)
    elsif (layerJson["__type__"].eql?("ShapeLayer"))
      extractShapeLayerInfo(layersArray, layerProperties, layerJson, fileHash)
    end
  end

  fileHash["animation_groups"] = animationGroups if animationGroups.length
  fileHash["features"] = layersArray
  puts @animationGroupIdHash
  return fileHash
end

# valuesJson = JSON.parse(File.open(ARGV[0], "r").read);
# puts JSON.pretty_generate(convertFunction(valuesJson))
# convertFunction(valuesJson)
