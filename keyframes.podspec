#
# Be sure to run `pod lib lint keyframes.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |spec|
  spec.name             = 'keyframes'
  spec.version          = '1.0.0'
  spec.license          = { :type => 'BSD', :file => 'LICENSE' }
  spec.homepage         = 'https://github.com/facebook/Keyframes'
  spec.author           = { 'Sean Lee' => 'landasiastudio@gmail.com' }
  spec.summary          = 'Vector+keyframe rendering framework for iOS'
  spec.source           = { :git => 'https://github.com/facebook/Keyframes.git', :tag => spec.version.to_s }
  spec.source_files     = 'ios/keyframes/src/**/*.{h,m,mm,cpp}'
  spec.public_header_files = 'ios/keyframes/src/{DataModel/KFVector,Layers/KFVectorLayer. ParsingHelpers/KFVectorParsingHelper,Views/KFVectorView,Helpers/KFUtilities}.h'
  spec.requires_arc = true
  spec.social_media_url = 'https://twitter.com/fbOpenSource'
  spec.ios.deployment_target = '8.0'
end
