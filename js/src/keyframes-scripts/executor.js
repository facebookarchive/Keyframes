const AECompToKeyframesAnimation = require('./AECompToKeyframesAnimation');

const args = process.argv.slice(2);
const fs = require('fs');
const parsedData = JSON.parse(fs.readFileSync(args[0], 'utf-8'));

const kfDoc = AECompToKeyframesAnimation(parsedData);
console.log(JSON.stringify(kfDoc, null, 2));

const requiredAsset = new Set();
kfDoc.features.forEach((feature) => {
  if (feature.backed_image) {
    requiredAsset.add(feature.backed_image);
  }
});

if (requiredAsset.size > 0) {
  console.error('');
  console.error('Please provide following image assets in your code: ');
  console.error('');
  console.error('Code template on iOS:');
  console.error('$KfVectorLayer$.imageAssets = @{');
  for (let imageName of requiredAsset) {
      console.error('  @"' + imageName + '": $UIImage$,');
  }
  console.error('};');
}
